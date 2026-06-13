package com.vtmer.microteachingmessage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vtmer.microteachingmessage.constant.MessageConsumeStatus;
import com.vtmer.microteachingmessage.mapper.MessageConsumeLogMapper;
import com.vtmer.microteachingmessage.pojo.MessageConsumeLog;
import com.vtmer.microteachingmessage.service.MessageConsumeLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class MessageConsumeLogServiceImpl implements MessageConsumeLogService {

    @Resource
    private MessageConsumeLogMapper messageConsumeLogMapper;

    @Override
    public void consumeOnce(MessageExt messageExt, String consumerGroup, Runnable runnable) {
        String msgKey = buildMsgKey(messageExt, consumerGroup);
        MessageConsumeLog messageConsumeLog = getByMsgKey(msgKey);
        if (messageConsumeLog != null && MessageConsumeStatus.SUCCESS.equals(messageConsumeLog.getStatus())) {
            log.info("消息已成功消费，直接跳过。msgKey={}", msgKey);
            return;
        }

        MessageConsumeLog processingLog = prepareProcessingLog(messageExt, consumerGroup, msgKey, messageConsumeLog);
        if (processingLog != null && MessageConsumeStatus.SUCCESS.equals(processingLog.getStatus())) {
            log.info("消息已被并发线程成功消费，直接跳过。msgKey={}", msgKey);
            return;
        }
        try {
            runnable.run();
            markSuccess(processingLog);
        } catch (RuntimeException e) {
            markFail(processingLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            markFail(processingLog, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private MessageConsumeLog prepareProcessingLog(MessageExt messageExt,
                                                   String consumerGroup,
                                                   String msgKey,
                                                   MessageConsumeLog existingLog) {
        LocalDateTime now = LocalDateTime.now();
        if (existingLog == null) {
            MessageConsumeLog messageConsumeLog = new MessageConsumeLog();
            messageConsumeLog.setMsgKey(msgKey);
            messageConsumeLog.setMsgId(messageExt.getMsgId());
            messageConsumeLog.setTopic(messageExt.getTopic());
            messageConsumeLog.setTag(messageExt.getTags());
            messageConsumeLog.setConsumerGroup(consumerGroup);
            messageConsumeLog.setStatus(MessageConsumeStatus.PROCESSING);
            messageConsumeLog.setRetryCount(0);
            messageConsumeLog.setCreateTime(now);
            messageConsumeLog.setUpdateTime(now);
            try {
                messageConsumeLogMapper.insert(messageConsumeLog);
                return messageConsumeLog;
            } catch (DuplicateKeyException duplicateKeyException) {
                return getByMsgKey(msgKey);
            }
        }
        existingLog.setStatus(MessageConsumeStatus.PROCESSING);
        existingLog.setRetryCount((existingLog.getRetryCount() == null ? 0 : existingLog.getRetryCount()) + 1);
        existingLog.setErrorMessage(null);
        existingLog.setUpdateTime(now);
        messageConsumeLogMapper.updateById(existingLog);
        return existingLog;
    }

    private void markSuccess(MessageConsumeLog messageConsumeLog) {
        messageConsumeLog.setStatus(MessageConsumeStatus.SUCCESS);
        messageConsumeLog.setErrorMessage(null);
        messageConsumeLog.setUpdateTime(LocalDateTime.now());
        messageConsumeLogMapper.updateById(messageConsumeLog);
    }

    private void markFail(MessageConsumeLog messageConsumeLog, String errorMessage) {
        messageConsumeLog.setStatus(MessageConsumeStatus.FAIL);
        messageConsumeLog.setErrorMessage(truncate(errorMessage));
        messageConsumeLog.setUpdateTime(LocalDateTime.now());
        messageConsumeLogMapper.updateById(messageConsumeLog);
    }

    private MessageConsumeLog getByMsgKey(String msgKey) {
        LambdaQueryWrapper<MessageConsumeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageConsumeLog::getMsgKey, msgKey).last("limit 1");
        return messageConsumeLogMapper.selectOne(queryWrapper);
    }

    private String buildMsgKey(MessageExt messageExt, String consumerGroup) {
        return messageExt.getTopic() + ":" + messageExt.getTags() + ":" + consumerGroup + ":" + messageExt.getMsgId();
    }

    private String truncate(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }
}
