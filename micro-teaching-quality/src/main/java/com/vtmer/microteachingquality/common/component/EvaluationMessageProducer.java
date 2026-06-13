package com.vtmer.microteachingquality.common.component;

import com.alibaba.fastjson2.JSON;
import com.vtmer.microteachingquality.common.constant.topic.ClazzEvaluationTopic;
import com.vtmer.microteachingquality.common.constant.topic.MajorEvaluationTopic;
import com.vtmer.microteachingquality.model.dto.UserMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class EvaluationMessageProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendClazzEvaluationMessage(String tag, Integer senderId, Long objectId) {
        send(ClazzEvaluationTopic.CLAZZ_EVALUATION, tag, senderId, objectId);
    }

    public void sendMajorEvaluationMessage(String tag, Integer senderId, Long objectId) {
        send(MajorEvaluationTopic.MAJOR_EVALUATION, tag, senderId, objectId);
    }

    private void send(String topic, String tag, Integer senderId, Long objectId) {
        String destination = topic + ":" + tag;
        String payload = JSON.toJSONString(new UserMessageDTO<>(senderId, objectId));
        rocketMQTemplate.syncSend(destination, payload);
        log.info("发送评审消息成功, destination={}, senderId={}, objectId={}", destination, senderId, objectId);
    }
}
