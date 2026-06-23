package com.vtmer.microteachingfile.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vtmer.microteachingfile.common.constant.UploadSessionStatus;
import com.vtmer.microteachingfile.model.pojo.UploadSession;
import com.vtmer.microteachingfile.service.FileStorageService;
import com.vtmer.microteachingfile.mapper.UploadSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class UploadSessionCleanupTask {

    @Resource
    private UploadSessionMapper uploadSessionMapper;
    @Resource
    private FileStorageService fileStorageService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void cleanupExpiredSessions() {
        LambdaQueryWrapper<UploadSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UploadSession::getStatus, UploadSessionStatus.INIT, UploadSessionStatus.UPLOADING, UploadSessionStatus.FAILED)
                .le(UploadSession::getExpireTime, LocalDateTime.now());
        List<UploadSession> sessions = uploadSessionMapper.selectList(queryWrapper);
        for (UploadSession session : sessions) {
            session.setStatus(UploadSessionStatus.EXPIRED);
            session.setUpdateTime(LocalDateTime.now());
            uploadSessionMapper.updateById(session);
            try {
                fileStorageService.deletePath(session.getSessionCode());
            } catch (IOException e) {
                log.warn("清理超时上传会话失败, sessionId={}, error={}", session.getId(), e.getMessage());
            }
        }
    }
}
