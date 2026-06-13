package com.vtmer.microteachingmessage.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.vtmer.microteachingmessage.mapper.UserMessageMapper;
import com.vtmer.microteachingmessage.pojo.UserMessage;
import com.vtmer.microteachingmessage.service.UserMessageService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Hung
 * @date 2022/7/27 0:20
 */
@Service
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private UserMessageMapper userMessageMapper;
    @Resource
    private Snowflake snowflake;

    @Override
    public Boolean insertOneMessage(UserMessage userMessage) {
        //先设置id
        userMessage.setId(snowflake.nextId());
        userMessage.setCreateTime(LocalDateTime.now());
        userMessage.setUpdateTime(LocalDateTime.now());
        userMessage.setReadTime(LocalDateTime.now());

        //找出最大用户信箱序列号
        Long size = redisTemplate.opsForList().size(userMessage.getRecipientId());
        UserMessage index;
        if (size == null || size == 0) {
            //redis找不到消息，可能是全部忽略或者信箱为空，则在数据库中查找最大序列号
            QueryWrapper<UserMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("sequence").eq("recipient_id", userMessage.getRecipientId());
            queryWrapper.orderByDesc("create_time");
            queryWrapper.ne("status", 1);
            index = userMessageMapper.selectOne(queryWrapper);
            if (index == null) {
                //数据库中没有消息，则序列号为1
                userMessage.setSequence(1);
            }
        } else {
            //redis找到消息，则序列号为最大序列号+1
            index = (UserMessage) redisTemplate.opsForList().index(userMessage.getRecipientId(), 0);
        }
        //设置序列号
        userMessage.setSequence(Objects.requireNonNull(index).getSequence() + 1);
        //插入Redis和数据库
        redisTemplate.opsForList().leftPush(userMessage.getRecipientId(), userMessage);

        return userMessage.insert();
    }

    @Override
    public Boolean insertBatchMessage(List<UserMessage> userMessageList) {
        //先设置id
        userMessageList.forEach(userMessage -> {
            userMessage.setId(snowflake.nextId());
            userMessage.setCreateTime(LocalDateTime.now());
            userMessage.setUpdateTime(LocalDateTime.now());
            userMessage.setReadTime(LocalDateTime.now());
        });

        //找出最大用户信箱序列号
        Long size = redisTemplate.opsForList().size(userMessageList.get(0).getRecipientId());
        UserMessage index;
        int sequence = 0;
        if (size == null || size == 0) {
            //redis找不到消息，可能是全部忽略或者信箱为空，则在数据库中查找最大序列号
            QueryWrapper<UserMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("sequence").eq("recipient_id", userMessageList.get(0).getRecipientId());
            queryWrapper.orderByDesc("create_time");
            queryWrapper.ne("status", 1);
            index = userMessageMapper.selectOne(queryWrapper);
            if (index == null) {
                //数据库中没有消息，则序列号为1
                sequence = 1;
            } else {
                sequence = index.getSequence() + 1;
            }
        } else {
            //redis找到消息，则序列号为最大序列号+1
            index = (UserMessage) redisTemplate.opsForList().index(userMessageList.get(0).getRecipientId(), 0);
            sequence = Objects.requireNonNull(index).getSequence() + 1;
        }
        //设置序列号
        for (int i = userMessageList.size() - 1; i >= 0; i--) {
            UserMessage message = userMessageList.get(i);
            message.setSequence(sequence++);
            userMessageList.set(i, message);
        }
        //插入Redis和数据库
        redisTemplate.opsForList().leftPushAll(userMessageList.get(0).getRecipientId(), userMessageList);
        userMessageList.forEach(Model::insert);
        return true;
    }

    @Override
    public Boolean deleteOneMessage(Long userMessageId, Integer recipientId) {
        //找出数据库中的消息
        UserMessage message = userMessageMapper.selectById(userMessageId);
        //更新状态
        userMessageMapper.updateMessageStatus(userMessageId);
        //删除redis中的消息
        redisTemplate.opsForList().remove(recipientId, 0, message);
        return true;
    }

    @Override
    public Boolean deleteBatchMessage(List<Long> userMessageList, Integer recipientId) {
        userMessageList.forEach(userMessageId -> {
            UserMessage message = userMessageMapper.selectById(userMessageId);
            userMessageMapper.updateMessageStatus(userMessageId);
            redisTemplate.opsForList().remove(recipientId, 0, message);
        });
        return true;
    }
}
