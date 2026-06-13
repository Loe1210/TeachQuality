package com.vtmer.microteachingmessage.listener;

import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingmessage.constant.ClazzEvaluationTopic;
import com.vtmer.microteachingmessage.exception.IllegalMessageTagException;
import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;
import com.vtmer.microteachingmessage.service.ClazzEvaluationProcessService;
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

    @Autowired
    private ClazzEvaluationProcessService clazzEvaluationProcessService;

    @Override
    public void onMessage(MessageExt messageExt) {

        String messageTag = messageExt.getTags();

        //解析消息内容
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        UserMessageDTO<?, ?> mqEntity = JSON.parseObject(body, UserMessageDTO.class);

        //根据消息标签进行不同处理
        switch (messageTag) {
            //创建课程评价流程
            case ClazzEvaluationTopic.PROCESS_CREATED:
                clazzEvaluationProcessService.evaluationProcessCreate(mqEntity);
                break;
            //课程负责人上传材料
            case ClazzEvaluationTopic.PRINCIPAL_UPLOAD:
                clazzEvaluationProcessService.evaluationProcessPrincipalUpload(mqEntity);
                break;
            //评审专家 退回材料
            case ClazzEvaluationTopic.MATERIAL_BACK:
                clazzEvaluationProcessService.evaluationProcessSendBackMaterial(mqEntity);
                break;
            //评审专家 提交评审
            case ClazzEvaluationTopic.EXPERT_SUBMIT:
                clazzEvaluationProcessService.evaluationProcessExpertSubmit(mqEntity);
                break;
            //评审专家组长 提交评审
            case ClazzEvaluationTopic.EXPERT_LEADER_SUBMIT:
                clazzEvaluationProcessService.evaluationProcessExpertLeaderSubmit(mqEntity);
                break;
            default:
                throw new IllegalMessageTagException("无MQ Tags");
        }

    }

}
