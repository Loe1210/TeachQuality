package com.hung.microoauth2auth.component;


import com.alibaba.fastjson.JSON;
import com.hung.microoauth2auth.entity.UserPrincipalDTO;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义JWT内容增强器
 *
 * @author Hung
 * @date 2021年11月15日 18:37
 */
@Component
public class JwtTokenEnhancer implements TokenEnhancer {

    private static final String PERMISSION_VERSION_PREFIX = "AUTH:PERMISSION_VERSION:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        UserPrincipalDTO userPrincipalDTO = JSON.parseObject(authentication.getName(), UserPrincipalDTO.class);
        Map<String, Object> info = new HashMap<>();
        //把用户ID设置到JWT中
        info.put("id", userPrincipalDTO.getId());
        info.put("permissionVersion", getPermissionVersion(userPrincipalDTO.getId()));
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
        return accessToken;
    }

    private long getPermissionVersion(Integer userId) {
        if (userId == null) {
            return 0L;
        }
        Object version = redisTemplate.opsForValue().get(PERMISSION_VERSION_PREFIX + userId);
        if (version instanceof Number) {
            return ((Number) version).longValue();
        }
        if (version instanceof String) {
            return Long.parseLong((String) version);
        }
        return 0L;
    }
}
