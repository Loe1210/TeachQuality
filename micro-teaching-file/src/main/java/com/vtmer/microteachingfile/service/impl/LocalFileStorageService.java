package com.vtmer.microteachingfile.service.impl;

import cn.hutool.core.io.FileUtil;
import com.vtmer.microteachingfile.config.properties.FileStorageProperties;
import com.vtmer.microteachingfile.service.FileStorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Resource
    private FileStorageProperties fileStorageProperties;

    @Override
    public String saveChunk(String sessionCode, Integer chunkIndex, InputStream inputStream) throws IOException {
        Path sessionDir = Path.of(fileStorageProperties.getTempDir(), sessionCode);
        Files.createDirectories(sessionDir);
        Path chunkPath = sessionDir.resolve("chunk-" + chunkIndex + ".part");
        Files.copy(inputStream, chunkPath, StandardCopyOption.REPLACE_EXISTING);
        return sessionCode + "/chunk-" + chunkIndex + ".part";
    }

    @Override
    public String mergeChunks(String storedName, List<Path> chunkPaths, String relativeObjectDir) throws IOException {
        Path objectDir = Path.of(fileStorageProperties.getObjectDir(), relativeObjectDir);
        Files.createDirectories(objectDir);
        Path objectPath = objectDir.resolve(storedName);
        Files.deleteIfExists(objectPath);
        try (OutputStream outputStream = Files.newOutputStream(objectPath)) {
            for (Path chunkPath : chunkPaths) {
                try (InputStream inputStream = Files.newInputStream(chunkPath)) {
                    inputStream.transferTo(outputStream);
                }
            }
        }
        return relativeObjectDir + "/" + storedName;
    }

    @Override
    public void deletePath(String storagePath) throws IOException {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        Path tempPath = Path.of(fileStorageProperties.getTempDir(), storagePath);
        Path objectPath = Path.of(fileStorageProperties.getObjectDir(), storagePath);
        if (Files.exists(tempPath)) {
            FileUtil.del(tempPath.toFile());
            return;
        }
        if (Files.exists(objectPath)) {
            FileUtil.del(objectPath.toFile());
            return;
        }
        Path rootPath = Path.of(fileStorageProperties.getRootDir(), storagePath);
        if (Files.exists(rootPath)) {
            FileUtil.del(rootPath.toFile());
        }
    }
}
