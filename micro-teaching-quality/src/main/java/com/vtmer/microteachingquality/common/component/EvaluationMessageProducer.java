package com.vtmer.microteachingquality.common.component;

import com.alibaba.fastjson2.JSON;
import com.vtmer.microteachingquality.common.constant.topic.ClazzEvaluationTopic;
import com.vtmer.microteachingquality.common.constant.topic.MajorEvaluationTopic;
import com.vtmer.microteachingquality.model.dto.UserMessageDTO;
import com.vtmer.microteachingquality.model.pojo.MessageLog;
import com.vtmer.microteachingquality.service.MessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

@Component
@Slf4j
public class EvaluationMessageProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private MessageLogService messageLogService;

    public void sendClazzEvaluationMessage(String tag, Integer senderId, Long objectId) {
        send(ClazzEvaluationTopic.CLAZZ_EVALUATION, tag, senderId, objectId);
    }

    public void sendMajorEvaluationMessage(String tag, Integer senderId, Long objectId) {
        send(MajorEvaluationTopic.MAJOR_EVALUATION, tag, senderId, objectId);
    }

    private void send(String topic, String tag, Integer senderId, Long objectId) {
        String destination = topic + ":" + tag;
        String payload = JSON.toJSONString(new UserMessageDTO<>(senderId, objectId));
        MessageLog messageLog = messageLogService.createPendingLog(topic, tag, destination, payload, senderId, objectId);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendLoggedMessage(messageLog.getId());
                }
            });
            return;
        }
        sendLoggedMessage(messageLog.getId());
    }

    public void sendLoggedMessage(Long logId) {
        MessageLog messageLog = messageLogService.getById(logId);
        if (messageLog == null) {
            return;
        }
        try {
            rocketMQTemplate.syncSend(messageLog.getDestination(), messageLog.getPayload());
            messageLogService.markSuccess(logId);
            log.info("发送评审消息成功, destination={}, senderId={}, objectId={}, logId={}",
                    messageLog.getDestination(), messageLog.getSenderId(), messageLog.getObjectId(), logId);
        } catch (Exception e) {
            messageLogService.markFailure(logId, e.getMessage());
            log.error("发送评审消息失败, destination={}, logId={}, error={}",
                    messageLog.getDestination(), logId, e.getMessage());
        }
    }
}
