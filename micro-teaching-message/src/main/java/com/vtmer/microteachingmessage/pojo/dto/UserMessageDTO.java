package com.vtmer.microteachingmessage.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Hung
 * @date 2022/5/25 0:44
 */
@AllArgsConstructor
@Data
public class UserMessageDTO<T, F> {
    /**
     * 通知提醒类型编号；三个0代表是系统发送的
     */
    T senderId;
    /**
     * 目标对象ID；
     */
    F objectId;

}
