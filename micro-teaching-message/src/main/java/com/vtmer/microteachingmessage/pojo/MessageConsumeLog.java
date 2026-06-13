package com.vtmer.microteachingmessage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("msg_consume_log")
public class MessageConsumeLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String msgKey;

    private String msgId;

    private String topic;

    private String tag;

    private String consumerGroup;

    private Integer status;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
