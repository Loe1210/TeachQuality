package com.vtmer.microteachingmessage.listener;

import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingmessage.constant.MajorEvaluationTopic;
import com.vtmer.microteachingmessage.exception.IllegalMessageTagException;
import com.vtmer.microteachingmessage.pojo.dto.UserMessageDTO;
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


    @Resource
    private MajorEvaluationProcessService majorEvaluationProcessService;

    @Override
    public void onMessage(MessageExt messageExt) {

        String messageTag = messageExt.getTags();

        //解析消息内容
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        UserMessageDTO<?, ?> mqEntity = JSON.parseObject(body, UserMessageDTO.class);

        //根据消息标签进行不同处理
        switch (messageTag) {
            //创建课程评价流程
            case MajorEvaluationTopic.PROCESS_CREATED:
                majorEvaluationProcessService.evaluationProcessCreate(mqEntity);
                break;
            //课程负责人上传材料
            case MajorEvaluationTopic.PRINCIPAL_UPLOAD:
                majorEvaluationProcessService.evaluationProcessPrincipalUpload(mqEntity);
                break;
            //评审专家 退回材料
            case MajorEvaluationTopic.MATERIAL_BACK:
                majorEvaluationProcessService.evaluationProcessSendBackMaterial(mqEntity);
                break;
            //评审专家 提交评审
            case MajorEvaluationTopic.EXPERT_SUBMIT:
                majorEvaluationProcessService.evaluationProcessExpertSubmit(mqEntity);
                break;
            //评审专家组长 提交评审
            case MajorEvaluationTopic.EXPERT_LEADER_SUBMIT:
                majorEvaluationProcessService.evaluationProcessExpertLeaderSubmit(mqEntity);
                break;
            default:
                throw new IllegalMessageTagException("无MQ Tags");
        }

    }


}
