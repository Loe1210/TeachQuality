package com.vtmer.microteachingmessage.service;

import com.vtmer.microteachingmessage.pojo.UserMessage;

import java.util.List;

/**
 * @author Hung
 * @date 2022/7/27 0:19
 */
public interface UserMessageService {

    Boolean insertOneMessage(UserMessage userMessage);

    Boolean insertBatchMessage(List<UserMessage> userMessageList);

    Boolean deleteOneMessage(Long id, Integer recipientId);

    Boolean deleteBatchMessage(List<Long> userMessageList, Integer recipientId);


}
