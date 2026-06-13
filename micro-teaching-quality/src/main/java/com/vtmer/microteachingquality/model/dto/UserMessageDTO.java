package com.vtmer.microteachingquality.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Hung
 * @date 2022/5/20 23:54
 */
@Data
@AllArgsConstructor
public class UserMessageDTO<T, F> {
    /**
     * 通知提醒类型编号；三个0代表是系统发送的
     */
    private T senderId;
    /**
     * 目标对象ID；
     */
    private F objectId;

}
