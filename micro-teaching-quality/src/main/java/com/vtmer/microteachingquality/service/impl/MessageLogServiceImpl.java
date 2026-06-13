package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vtmer.microteachingquality.common.constant.MessageLogStatus;
import com.vtmer.microteachingquality.mapper.MessageLogMapper;
import com.vtmer.microteachingquality.model.pojo.MessageLog;
import com.vtmer.microteachingquality.service.MessageLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageLogServiceImpl implements MessageLogService {

    private static final int MAX_RETRY_COUNT = 5;

    @Resource
    private MessageLogMapper messageLogMapper;

    @Override
    public MessageLog createPendingLog(String topic, String tag, String destination, String payload, Integer senderId, Long objectId) {
        LocalDateTime now = LocalDateTime.now();
        MessageLog messageLog = new MessageLog();
        messageLog.setTopic(topic);
        messageLog.setTag(tag);
        messageLog.setDestination(destination);
        messageLog.setPayload(payload);
        messageLog.setSenderId(senderId);
        messageLog.setObjectId(objectId);
        messageLog.setStatus(MessageLogStatus.PENDING);
        messageLog.setRetryCount(0);
        messageLog.setNextRetryTime(now);
        messageLog.setCreateTime(now);
        messageLog.setUpdateTime(now);
        messageLogMapper.insert(messageLog);
        return messageLog;
    }

    @Override
    public void markSuccess(Long logId) {
        MessageLog messageLog = messageLogMapper.selectById(logId);
        if (messageLog == null) {
            return;
        }
        messageLog.setStatus(MessageLogStatus.SUCCESS);
        messageLog.setErrorMessage(null);
        messageLog.setUpdateTime(LocalDateTime.now());
        messageLogMapper.updateById(messageLog);
    }

    @Override
    public void markFailure(Long logId, String errorMessage) {
        MessageLog messageLog = messageLogMapper.selectById(logId);
        if (messageLog == null) {
            return;
        }
        int retryCount = messageLog.getRetryCount() == null ? 0 : messageLog.getRetryCount();
        retryCount++;
        messageLog.setRetryCount(retryCount);
        messageLog.setStatus(MessageLogStatus.FAIL);
        messageLog.setErrorMessage(truncateError(errorMessage));
        messageLog.setNextRetryTime(LocalDateTime.now().plusMinutes(Math.min(retryCount, MAX_RETRY_COUNT)));
        messageLog.setUpdateTime(LocalDateTime.now());
        messageLogMapper.updateById(messageLog);
    }

    @Override
    public List<MessageLog> listRetryableLogs(int limit) {
        LambdaQueryWrapper<MessageLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .in(MessageLog::getStatus, MessageLogStatus.PENDING, MessageLogStatus.FAIL)
                .lt(MessageLog::getRetryCount, MAX_RETRY_COUNT)
                .le(MessageLog::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(MessageLog::getNextRetryTime)
                .last("limit " + limit);
        return messageLogMapper.selectList(queryWrapper);
    }

    @Override
    public MessageLog getById(Long logId) {
        return messageLogMapper.selectById(logId);
    }

    private String truncateError(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }
}
