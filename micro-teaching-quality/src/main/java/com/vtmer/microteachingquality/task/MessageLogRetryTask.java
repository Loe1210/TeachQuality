package com.vtmer.microteachingquality.task;

import com.vtmer.microteachingquality.common.component.EvaluationMessageProducer;
import com.vtmer.microteachingquality.model.pojo.MessageLog;
import com.vtmer.microteachingquality.service.MessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class MessageLogRetryTask {

    private static final int RETRY_BATCH_SIZE = 50;

    @Resource
    private MessageLogService messageLogService;
    @Resource
    private EvaluationMessageProducer evaluationMessageProducer;

    @Scheduled(fixedDelay = 60000)
    public void retryFailedMessages() {
        List<MessageLog> retryableLogs = messageLogService.listRetryableLogs(RETRY_BATCH_SIZE);
        if (retryableLogs.isEmpty()) {
            return;
        }
        log.info("开始补偿重试消息, count={}", retryableLogs.size());
        retryableLogs.forEach(messageLog -> evaluationMessageProducer.sendLoggedMessage(messageLog.getId()));
    }
}
