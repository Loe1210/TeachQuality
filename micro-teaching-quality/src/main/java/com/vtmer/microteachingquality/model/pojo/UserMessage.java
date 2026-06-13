package com.vtmer.microteachingquality.model.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/5/19 21:29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserMessage extends Model<UserMessage> {
    /**
     * 消息Id 在消息队列后生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    Long id;
    /**
     * 用户收件箱序列号
     */
    Integer sequence;
    /**
     * 通知提醒类型编号；
     */
    Integer remindId;
    /**
     * 通知提醒类型编号；三个0代表是系统发送的
     */
    Integer senderId;
    /**
     * 操作者用户名；
     */
    String senderName;
    /**
     * 操作者的动作，如：赞了、评论了、喜欢了、捐款了、收藏了；
     */
    String senderAction;
    /**
     * 目标对象ID；
     */
    Long objectId;
    /**
     * 目标对象内容或简介，比如：文章标题；
     */
    String object;
    /**
     * 被操作对象类型，如：人、文章、活动、视频等；
     */
    String objectType;
    /**
     * 消息接收者；可能是对象的所有者或订阅者；
     */
    Integer recipientId;
    /**
     * 消息内容，由提醒模版生成，需要提前定义；
     */
    String message;
    /**
     * 是否阅读，默认为0，忽略为1 ；
     */
    Integer status;
    /**
     * 阅读时间；暂时不用理会 和 updateTime一样就行
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime readTime;
    @TableField(fill = FieldFill.INSERT)
    LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime updateTime;

    public UserMessage(Integer remindId, Integer senderId, String senderName, String senderAction, Long objectId, String object, String objectType, Integer recipientId, String message) {
        this.remindId = remindId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAction = senderAction;
        this.objectId = objectId;
        this.object = object;
        this.objectType = objectType;
        this.recipientId = recipientId;
        this.message = message;
        this.status = 0;
    }
}
