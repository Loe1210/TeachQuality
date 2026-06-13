package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.pojo.MessageLog;

import java.util.List;

public interface MessageLogService {

    MessageLog createPendingLog(String topic, String tag, String destination, String payload, Integer senderId, Long objectId);

    void markSuccess(Long logId);

    void markFailure(Long logId, String errorMessage);

    List<MessageLog> listRetryableLogs(int limit);

    MessageLog getById(Long logId);
}
