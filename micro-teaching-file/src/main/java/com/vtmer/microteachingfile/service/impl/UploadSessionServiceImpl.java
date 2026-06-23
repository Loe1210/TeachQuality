package com.vtmer.microteachingfile.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vtmer.microteachingfile.common.exception.CustomException;
import com.vtmer.microteachingfile.common.constant.FileObjectStatus;
import com.vtmer.microteachingfile.common.constant.UploadChunkStatus;
import com.vtmer.microteachingfile.common.constant.UploadSessionStatus;
import com.vtmer.microteachingfile.common.util.FileHashUtil;
import com.vtmer.microteachingfile.common.util.FileTypeDetector;
import com.vtmer.microteachingfile.config.properties.FileStorageProperties;
import com.vtmer.microteachingfile.config.properties.FileUploadProperties;
import com.vtmer.microteachingfile.mapper.FileObjectMapper;
import com.vtmer.microteachingfile.mapper.UploadChunkMapper;
import com.vtmer.microteachingfile.mapper.UploadSessionMapper;
import com.vtmer.microteachingfile.model.dto.CompleteUploadSessionRequest;
import com.vtmer.microteachingfile.model.dto.CreateUploadSessionRequest;
import com.vtmer.microteachingfile.model.pojo.FileObject;
import com.vtmer.microteachingfile.model.pojo.UploadChunk;
import com.vtmer.microteachingfile.model.pojo.UploadSession;
import com.vtmer.microteachingfile.model.vo.CreateUploadSessionResponse;
import com.vtmer.microteachingfile.model.vo.FileObjectResponse;
import com.vtmer.microteachingfile.model.vo.UploadSessionDetailResponse;
import com.vtmer.microteachingfile.service.FileStorageService;
import com.vtmer.microteachingfile.service.UploadSessionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UploadSessionServiceImpl implements UploadSessionService {

    @Resource
    private UploadSessionMapper uploadSessionMapper;
    @Resource
    private UploadChunkMapper uploadChunkMapper;
    @Resource
    private FileObjectMapper fileObjectMapper;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private FileUploadProperties fileUploadProperties;
    @Resource
    private FileStorageProperties fileStorageProperties;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateUploadSessionResponse createSession(CreateUploadSessionRequest request) {
        String lockKey = "file:upload:user:create:" + request.getCreateUserId();
        return executeWithLock(lockKey, () -> {
            validateCreateRequest(request);
            FileObject existingObject = findReadyFileObjectByHash(request.getFileHash());
            if (existingObject != null) {
                return CreateUploadSessionResponse.builder()
                        .existingFileObjectId(existingObject.getId())
                        .uploadedChunks(new ArrayList<>())
                        .chunkSize(resolveChunkSize(request.getChunkSize()))
                        .totalChunks(request.getTotalChunks())
                        .build();
            }
            ensureConcurrentSessionLimit(request.getCreateUserId());
            UploadSession session = new UploadSession();
            LocalDateTime now = LocalDateTime.now();
            session.setSessionCode(IdUtil.fastSimpleUUID());
            session.setBizType(request.getBizType());
            session.setBizId(request.getBizId());
            session.setOriginalName(request.getOriginalName());
            session.setExpectedSize(request.getExpectedSize());
            session.setChunkSize(resolveChunkSize(request.getChunkSize()));
            session.setTotalChunks(request.getTotalChunks());
            session.setUploadedChunks(0);
            session.setClientFileHash(request.getFileHash());
            session.setContentType(request.getContentType());
            session.setStatus(UploadSessionStatus.INIT);
            session.setTempDir(session.getSessionCode());
            session.setCompleteRetryCount(0);
            session.setExpireTime(now.plusMinutes(fileUploadProperties.getSessionExpireMinutes()));
            session.setLastChunkTime(now);
            session.setCreateUserId(request.getCreateUserId());
            session.setCreateTime(now);
            session.setUpdateTime(now);
            uploadSessionMapper.insert(session);
            return CreateUploadSessionResponse.builder()
                    .sessionId(session.getId())
                    .sessionCode(session.getSessionCode())
                    .chunkSize(session.getChunkSize())
                    .totalChunks(session.getTotalChunks())
                    .uploadedChunks(new ArrayList<>())
                    .build();
        });
    }

    @Override
    public UploadSessionDetailResponse getSession(Long sessionId) {
        UploadSession session = getSessionOrThrow(sessionId);
        return buildSessionDetail(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadSessionDetailResponse uploadChunk(Long sessionId, Integer chunkIndex, MultipartFile file, String chunkHash, Integer operatorUserId) {
        return executeWithSessionLock(sessionId, () -> {
            UploadSession session = getSessionOrThrow(sessionId);
            validateChunkUpload(session, chunkIndex, file, operatorUserId);
            String storagePath;
            try {
                storagePath = fileStorageService.saveChunk(session.getSessionCode(), chunkIndex, file.getInputStream());
                verifyChunkHash(chunkHash, storagePath);
            } catch (IOException e) {
                throw new CustomException("分片上传失败");
            }

            UploadChunk chunk = getChunk(sessionId, chunkIndex);
            LocalDateTime now = LocalDateTime.now();
            if (chunk == null) {
                chunk = new UploadChunk();
                chunk.setSessionId(sessionId);
                chunk.setChunkIndex(chunkIndex);
                chunk.setCreateTime(now);
                chunk.setRetryCount(0);
            } else {
                chunk.setRetryCount((chunk.getRetryCount() == null ? 0 : chunk.getRetryCount()) + 1);
            }
            chunk.setChunkSize(file.getSize());
            chunk.setChunkHash(chunkHash);
            chunk.setStoragePath(storagePath);
            chunk.setStatus(UploadChunkStatus.UPLOADED);
            chunk.setUploadTime(now);
            chunk.setUpdateTime(now);
            if (chunk.getId() == null) {
                uploadChunkMapper.insert(chunk);
            } else {
                uploadChunkMapper.updateById(chunk);
            }

            session.setStatus(UploadSessionStatus.UPLOADING);
            session.setUploadedChunks(countUploadedChunks(sessionId));
            session.setLastChunkTime(now);
            session.setUpdateTime(now);
            session.setExpireTime(now.plusMinutes(fileUploadProperties.getSessionExpireMinutes()));
            uploadSessionMapper.updateById(session);
            return buildSessionDetail(session);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileObjectResponse completeSession(Long sessionId, CompleteUploadSessionRequest request) {
        return executeWithSessionLock(sessionId, () -> {
            UploadSession session = getSessionOrThrow(sessionId);
            if (!session.getCreateUserId().equals(request.getOperatorUserId())) {
                throw new CustomException("无权完成该上传会话");
            }
            if (session.getStatus().equals(UploadSessionStatus.COMPLETED)) {
                if (session.getFileObjectId() == null) {
                    throw new CustomException("上传会话状态异常，缺少文件对象");
                }
                return getFileObject(session.getFileObjectId());
            }
            ensureSessionActive(session);
            List<UploadChunk> chunks = listChunks(sessionId);
            if (chunks.size() != session.getTotalChunks()) {
                throw new CustomException("分片未上传完整，无法合并");
            }
            session.setStatus(UploadSessionStatus.MERGING);
            session.setUpdateTime(LocalDateTime.now());
            uploadSessionMapper.updateById(session);

            String storagePath = null;
            try {
                String relativeDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String storedName = IdUtil.fastSimpleUUID() + "." + FileTypeDetector.getExtension(session.getOriginalName());
                List<Path> chunkPaths = chunks.stream()
                        .sorted(Comparator.comparingInt(UploadChunk::getChunkIndex))
                        .map(chunk -> Path.of(fileStorageProperties.getTempDir(), chunk.getStoragePath()))
                        .collect(Collectors.toList());
                storagePath = fileStorageService.mergeChunks(storedName, chunkPaths, relativeDir);
                Path mergedPath = Path.of(fileStorageProperties.getObjectDir(), storagePath);
                String serverHash = FileHashUtil.sha256(mergedPath);
                validateMergedFile(session, mergedPath, serverHash);

                FileObject existingObject = findReadyFileObjectByHash(serverHash);
                FileObject fileObject;
                if (existingObject != null) {
                    fileStorageService.deletePath(storagePath);
                    fileObject = existingObject;
                } else {
                    fileObject = buildFileObject(session, storedName, storagePath, serverHash, Files.size(mergedPath));
                    fileObjectMapper.insert(fileObject);
                }
                markChunksMerged(chunks);
                session.setServerFileHash(serverHash);
                session.setFileObjectId(fileObject.getId());
                session.setUploadedChunks(session.getTotalChunks());
                session.setStatus(UploadSessionStatus.COMPLETED);
                session.setUpdateTime(LocalDateTime.now());
                uploadSessionMapper.updateById(session);
                fileStorageService.deletePath(session.getSessionCode());
                return buildFileObjectResponse(fileObject);
            } catch (Exception e) {
                if (storagePath != null) {
                    try {
                        fileStorageService.deletePath(storagePath);
                    } catch (IOException ignored) {
                    }
                }
                session.setCompleteRetryCount((session.getCompleteRetryCount() == null ? 0 : session.getCompleteRetryCount()) + 1);
                session.setStatus(session.getCompleteRetryCount() >= fileUploadProperties.getMaxCompleteRetryCount()
                        ? UploadSessionStatus.FAILED : UploadSessionStatus.UPLOADING);
                session.setUpdateTime(LocalDateTime.now());
                uploadSessionMapper.updateById(session);
                if (e instanceof CustomException) {
                    throw (CustomException) e;
                }
                throw new CustomException("文件合并失败");
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSession(Long sessionId, Integer operatorUserId) {
        executeWithSessionLock(sessionId, () -> {
            UploadSession session = getSessionOrThrow(sessionId);
            if (!session.getCreateUserId().equals(operatorUserId)) {
                throw new CustomException("无权取消该上传会话");
            }
            session.setStatus(UploadSessionStatus.CANCELED);
            session.setUpdateTime(LocalDateTime.now());
            uploadSessionMapper.updateById(session);
            try {
                fileStorageService.deletePath(session.getSessionCode());
            } catch (IOException e) {
                throw new CustomException("清理临时分片失败");
            }
            return null;
        });
    }

    @Override
    public FileObjectResponse getFileObject(Long fileObjectId) {
        FileObject fileObject = fileObjectMapper.selectById(fileObjectId);
        if (fileObject == null) {
            throw new CustomException("文件对象不存在");
        }
        return buildFileObjectResponse(fileObject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileObject(Long fileObjectId, Integer operatorUserId) {
        FileObject fileObject = fileObjectMapper.selectById(fileObjectId);
        if (fileObject == null) {
            throw new CustomException("文件对象不存在");
        }
        if (!fileObject.getCreateUserId().equals(operatorUserId)) {
            throw new CustomException("无权删除该文件对象");
        }
        fileObject.setStatus(FileObjectStatus.DELETED);
        fileObject.setUpdateTime(LocalDateTime.now());
        fileObjectMapper.updateById(fileObject);
        try {
            fileStorageService.deletePath(fileObject.getStoragePath());
        } catch (IOException e) {
            throw new CustomException("删除文件失败");
        }
    }

    private void validateCreateRequest(CreateUploadSessionRequest request) {
        String extension = FileTypeDetector.getExtension(request.getOriginalName());
        if (!fileUploadProperties.getAllowedExtensions().stream()
                .map(item -> item.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet())
                .contains(extension)) {
            throw new CustomException("文件类型不允许上传");
        }
        if (request.getExpectedSize() > fileUploadProperties.getMaxFileSizeBytes()) {
            throw new CustomException("文件超出上传大小限制");
        }
        long chunkSize = resolveChunkSize(request.getChunkSize());
        long expectedChunkCount = (request.getExpectedSize() + chunkSize - 1) / chunkSize;
        if (expectedChunkCount != request.getTotalChunks()) {
            throw new CustomException("分片总数与文件大小不匹配");
        }
    }

    private void ensureConcurrentSessionLimit(Integer createUserId) {
        LambdaQueryWrapper<UploadSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadSession::getCreateUserId, createUserId)
                .in(UploadSession::getStatus, UploadSessionStatus.INIT, UploadSessionStatus.UPLOADING, UploadSessionStatus.MERGING);
        Long count = uploadSessionMapper.selectCount(queryWrapper);
        if (count != null && count >= fileUploadProperties.getMaxConcurrentSessionsPerUser()) {
            throw new CustomException("当前并发上传会话过多，请稍后重试");
        }
    }

    private Long resolveChunkSize(Long chunkSize) {
        if (chunkSize == null || chunkSize <= 0) {
            return fileUploadProperties.getDefaultChunkSizeBytes();
        }
        return chunkSize;
    }

    private UploadSession getSessionOrThrow(Long sessionId) {
        UploadSession session = uploadSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new CustomException("上传会话不存在");
        }
        return session;
    }

    private UploadChunk getChunk(Long sessionId, Integer chunkIndex) {
        LambdaQueryWrapper<UploadChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadChunk::getSessionId, sessionId)
                .eq(UploadChunk::getChunkIndex, chunkIndex)
                .last("limit 1");
        return uploadChunkMapper.selectOne(queryWrapper);
    }

    private List<UploadChunk> listChunks(Long sessionId) {
        LambdaQueryWrapper<UploadChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadChunk::getSessionId, sessionId);
        return uploadChunkMapper.selectList(queryWrapper);
    }

    private int countUploadedChunks(Long sessionId) {
        LambdaQueryWrapper<UploadChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadChunk::getSessionId, sessionId)
                .eq(UploadChunk::getStatus, UploadChunkStatus.UPLOADED);
        Long count = uploadChunkMapper.selectCount(queryWrapper);
        return count == null ? 0 : count.intValue();
    }

    private void validateChunkUpload(UploadSession session, Integer chunkIndex, MultipartFile file, Integer operatorUserId) {
        if (!session.getCreateUserId().equals(operatorUserId)) {
            throw new CustomException("无权上传该分片");
        }
        ensureSessionActive(session);
        if (session.getStatus().equals(UploadSessionStatus.CANCELED)
                || session.getStatus().equals(UploadSessionStatus.COMPLETED)
                || session.getStatus().equals(UploadSessionStatus.EXPIRED)) {
            throw new CustomException("当前上传会话状态不允许继续上传");
        }
        if (chunkIndex == null || chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
            throw new CustomException("分片下标非法");
        }
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传分片不能为空");
        }
        long expectedChunkSize = resolveExpectedChunkSize(session, chunkIndex);
        if (file.getSize() > expectedChunkSize) {
            throw new CustomException("分片大小超出预期");
        }
    }

    private UploadSessionDetailResponse buildSessionDetail(UploadSession session) {
        List<Integer> uploadedIndexes = listChunks(session.getId()).stream()
                .filter(chunk -> UploadChunkStatus.UPLOADED.equals(chunk.getStatus()) || UploadChunkStatus.MERGED.equals(chunk.getStatus()))
                .map(UploadChunk::getChunkIndex)
                .sorted()
                .collect(Collectors.toList());
        return UploadSessionDetailResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .bizType(session.getBizType())
                .bizId(session.getBizId())
                .originalName(session.getOriginalName())
                .expectedSize(session.getExpectedSize())
                .chunkSize(session.getChunkSize())
                .totalChunks(session.getTotalChunks())
                .uploadedChunks(session.getUploadedChunks())
                .status(session.getStatus())
                .uploadedChunkIndexes(uploadedIndexes)
                .expireTime(session.getExpireTime())
                .fileObjectId(session.getFileObjectId())
                .build();
    }

    private void validateMergedFile(UploadSession session, Path mergedPath, String serverHash) throws IOException {
        if (session.getExpectedSize() != null && Files.size(mergedPath) != session.getExpectedSize()) {
            throw new CustomException("文件大小校验失败");
        }
        if (StrUtil.isNotBlank(session.getClientFileHash()) && !session.getClientFileHash().equalsIgnoreCase(serverHash)) {
            throw new CustomException("文件 hash 校验失败");
        }
        String extension = FileTypeDetector.getExtension(session.getOriginalName());
        if (!fileUploadProperties.getAllowedExtensions().contains(extension)) {
            throw new CustomException("文件扩展名不允许上传");
        }
        String magicExtension = FileTypeDetector.detectMagicExtension(mergedPath);
        if (!FileTypeDetector.matchesExpected(session.getOriginalName(), magicExtension)) {
            throw new CustomException("文件魔数校验失败");
        }
        String contentType = Files.probeContentType(mergedPath);
        if (StrUtil.isNotBlank(session.getContentType()) && StrUtil.isNotBlank(contentType)) {
            String expectedType = session.getContentType().toLowerCase(Locale.ROOT);
            String actualType = contentType.toLowerCase(Locale.ROOT);
            if (!actualType.contains(extension) && !expectedType.equals(actualType) && !actualType.startsWith(expectedType.split("/")[0] + "/")) {
                throw new CustomException("文件 MIME 校验失败");
            }
        }
    }

    private void verifyChunkHash(String chunkHash, String storagePath) throws IOException {
        if (StrUtil.isBlank(chunkHash)) {
            return;
        }
        Path chunkPath = Path.of(fileStorageProperties.getTempDir(), storagePath);
        String serverChunkHash = FileHashUtil.sha256(chunkPath);
        if (!chunkHash.equalsIgnoreCase(serverChunkHash)) {
            throw new CustomException("分片 hash 校验失败");
        }
    }

    private long resolveExpectedChunkSize(UploadSession session, Integer chunkIndex) {
        if (chunkIndex == session.getTotalChunks() - 1) {
            long consumed = session.getChunkSize() * (session.getTotalChunks() - 1L);
            return session.getExpectedSize() - consumed;
        }
        return session.getChunkSize();
    }

    private void ensureSessionActive(UploadSession session) {
        if (session.getExpireTime() != null && session.getExpireTime().isBefore(LocalDateTime.now())) {
            session.setStatus(UploadSessionStatus.EXPIRED);
            session.setUpdateTime(LocalDateTime.now());
            uploadSessionMapper.updateById(session);
            throw new CustomException("上传会话已过期，请重新发起上传");
        }
    }

    private FileObject buildFileObject(UploadSession session, String storedName, String storagePath, String serverHash, long fileSize) {
        FileObject fileObject = new FileObject();
        LocalDateTime now = LocalDateTime.now();
        fileObject.setBizType(session.getBizType());
        fileObject.setBizId(session.getBizId());
        fileObject.setOriginalName(session.getOriginalName());
        fileObject.setStoredName(storedName);
        fileObject.setStoragePath(storagePath);
        fileObject.setStorageProvider("local");
        fileObject.setFileSize(fileSize);
        fileObject.setContentType(session.getContentType());
        fileObject.setFileHash(serverHash);
        fileObject.setStatus(FileObjectStatus.READY);
        fileObject.setCreateUserId(session.getCreateUserId());
        fileObject.setCreateTime(now);
        fileObject.setUpdateTime(now);
        return fileObject;
    }

    private void markChunksMerged(List<UploadChunk> chunks) {
        LocalDateTime now = LocalDateTime.now();
        for (UploadChunk chunk : chunks) {
            chunk.setStatus(UploadChunkStatus.MERGED);
            chunk.setUpdateTime(now);
            uploadChunkMapper.updateById(chunk);
        }
    }

    private FileObject findReadyFileObjectByHash(String fileHash) {
        if (StrUtil.isBlank(fileHash)) {
            return null;
        }
        LambdaQueryWrapper<FileObject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileObject::getFileHash, fileHash)
                .eq(FileObject::getStatus, FileObjectStatus.READY)
                .last("limit 1");
        return fileObjectMapper.selectOne(queryWrapper);
    }

    private FileObjectResponse buildFileObjectResponse(FileObject fileObject) {
        return FileObjectResponse.builder()
                .id(fileObject.getId())
                .bizType(fileObject.getBizType())
                .bizId(fileObject.getBizId())
                .originalName(fileObject.getOriginalName())
                .storagePath(fileObject.getStoragePath())
                .storageProvider(fileObject.getStorageProvider())
                .fileSize(fileObject.getFileSize())
                .contentType(fileObject.getContentType())
                .fileHash(fileObject.getFileHash())
                .status(fileObject.getStatus())
                .build();
    }

    private <T> T executeWithSessionLock(Long sessionId, FileAction<T> action) {
        return executeWithLock("file:upload:session:" + sessionId, action);
    }

    private <T> T executeWithLock(String lockKey, FileAction<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new CustomException("当前文件操作繁忙，请稍后重试");
            }
            return action.execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException("文件操作被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    private interface FileAction<T> {
        T execute();
    }
}
