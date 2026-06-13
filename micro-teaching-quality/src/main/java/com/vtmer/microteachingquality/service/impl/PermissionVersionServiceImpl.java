package com.vtmer.microteachingquality.service.impl;

import com.vtmer.microteachingquality.auth.constant.RedisConstant;
import com.vtmer.microteachingquality.service.PermissionVersionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PermissionVersionServiceImpl implements PermissionVersionService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void bump(Integer userId) {
        if (userId == null) {
            return;
        }
        stringRedisTemplate.opsForValue().increment(RedisConstant.PERMISSION_VERSION_PREFIX + userId);
    }
}
