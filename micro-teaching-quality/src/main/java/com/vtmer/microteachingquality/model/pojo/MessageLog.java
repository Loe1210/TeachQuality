package com.vtmer.microteachingquality.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("msg_log")
public class MessageLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String topic;

    private String tag;

    private String destination;

    private String payload;

    private Integer senderId;

    private Long objectId;

    /**
     * 0-待发送 1-发送成功 2-发送失败
     */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
