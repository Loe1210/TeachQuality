package com.vtmer.microteachingmessage.service;

import org.apache.rocketmq.common.message.MessageExt;

public interface MessageConsumeLogService {

    void consumeOnce(MessageExt messageExt, String consumerGroup, Runnable runnable);
}
