package com.vtmer.microteachingmessage.listener;

import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingmessage.constant.ClazzEvaluationTopic;
import com.vtmer.microteachingmessage.exception.IllegalMessageTagException;
import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;
import com.vtmer.microteachingmessage.service.ClazzEvaluationProcessService;
import com.vtmer.microteachingmessage.service.MessageConsumeLogService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author Hung
 * @date 2022/5/12 17:09
 */
@Component
@RocketMQMessageListener(topic = "clazz_evaluation", consumerGroup = "clazz_message_group", messageModel = MessageModel.CLUSTERING)
public class ClazzEvaluationProcessListener implements RocketMQListener<MessageExt> {

    private static final String CONSUMER_GROUP = "clazz_message_group";

    @Autowired
    private ClazzEvaluationProcessService clazzEvaluationProcessService;
    @Autowired
    private MessageConsumeLogService messageConsumeLogService;

    @Override
    public void onMessage(MessageExt messageExt) {
        messageConsumeLogService.consumeOnce(messageExt, CONSUMER_GROUP, () -> handleMessage(messageExt));
    }

    private void handleMessage(MessageExt messageExt) {
        String messageTag = messageExt.getTags();
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        UserMessageDTO<?, ?> mqEntity = JSON.parseObject(body, UserMessageDTO.class);

        switch (messageTag) {
            case ClazzEvaluationTopic.PROCESS_CREATED:
                clazzEvaluationProcessService.evaluationProcessCreate(mqEntity);
                return;
            case ClazzEvaluationTopic.PRINCIPAL_UPLOAD:
                clazzEvaluationProcessService.evaluationProcessPrincipalUpload(mqEntity);
                return;
            case ClazzEvaluationTopic.MATERIAL_BACK:
                clazzEvaluationProcessService.evaluationProcessSendBackMaterial(mqEntity);
                return;
            case ClazzEvaluationTopic.EXPERT_SUBMIT:
                clazzEvaluationProcessService.evaluationProcessExpertSubmit(mqEntity);
                return;
            case ClazzEvaluationTopic.EXPERT_LEADER_SUBMIT:
                clazzEvaluationProcessService.evaluationProcessExpertLeaderSubmit(mqEntity);
                return;
            default:
                throw new IllegalMessageTagException("无MQ Tags");
        }
    }

}
