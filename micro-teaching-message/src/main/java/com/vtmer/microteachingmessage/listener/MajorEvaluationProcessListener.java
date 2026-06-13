package com.vtmer.microteachingmessage.listener;

import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingmessage.constant.MajorEvaluationTopic;
import com.vtmer.microteachingmessage.exception.IllegalMessageTagException;
import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;
import com.vtmer.microteachingmessage.service.MessageConsumeLogService;
import com.vtmer.microteachingmessage.service.MajorEvaluationProcessService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @author Hung
 * @date 2022/8/16 16:33
 */
@Component
@RocketMQMessageListener(topic = "major_evaluation", consumerGroup = "major_message_group", messageModel = MessageModel.CLUSTERING)
public class MajorEvaluationProcessListener implements RocketMQListener<MessageExt> {

    private static final String CONSUMER_GROUP = "major_message_group";

    @Resource
    private MajorEvaluationProcessService majorEvaluationProcessService;
    @Resource
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
            case MajorEvaluationTopic.PROCESS_CREATED:
                majorEvaluationProcessService.evaluationProcessCreate(mqEntity);
                return;
            case MajorEvaluationTopic.PRINCIPAL_UPLOAD:
                majorEvaluationProcessService.evaluationProcessPrincipalUpload(mqEntity);
                return;
            case MajorEvaluationTopic.MATERIAL_BACK:
                majorEvaluationProcessService.evaluationProcessSendBackMaterial(mqEntity);
                return;
            case MajorEvaluationTopic.EXPERT_SUBMIT:
                majorEvaluationProcessService.evaluationProcessExpertSubmit(mqEntity);
                return;
            case MajorEvaluationTopic.EXPERT_LEADER_SUBMIT:
                majorEvaluationProcessService.evaluationProcessExpertLeaderSubmit(mqEntity);
                return;
            default:
                throw new IllegalMessageTagException("无MQ Tags");
        }
    }

}
