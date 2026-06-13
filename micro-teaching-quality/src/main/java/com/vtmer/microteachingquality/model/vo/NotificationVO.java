package com.vtmer.microteachingquality.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : Gking
 * @date : 2022-04-26 20:42
 **/
@Data
public class NotificationVO {
    private LocalDateTime createTime;

    private String content;
}
