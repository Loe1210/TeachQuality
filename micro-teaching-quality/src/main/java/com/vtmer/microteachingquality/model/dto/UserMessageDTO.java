package com.vtmer.microteachingquality.model.dto;

import java.nio.charset.StandardCharsets;

/**
 * @author Hung
 * @date 2022/5/20 23:54
 */
public class UserMessageDTO<T, F> {
    /**
     * 通知提醒类型编号；三个0代表是系统发送的
     */
    T senderId;
    /**
     * 目标对象ID；
     */
    F objectId;

    public UserMessageDTO(T senderId, F objectId) {
        this.senderId = senderId;
        this.objectId = objectId;
    }

    public static byte[] newInstance(UserMessageDTO<?, ?> userMessageDTO) {
        return userMessageDTO.toString().getBytes(StandardCharsets.UTF_8);
    }

}
