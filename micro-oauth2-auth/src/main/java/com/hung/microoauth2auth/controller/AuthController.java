package com.hung.microoauth2auth.controller;

import com.hung.microoauth2auth.entity.Oauth2TokenDTO;
import com.hung.microoauth2commons.commonutils.api.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义Oauth2获取令牌接口
 *
 * @author Hung
 * @date 2021年11月23日 10:42:10
 */
@RestController
@RequestMapping("/oauth")
public class AuthController {

    private static final String AUTH_TOKEN_BLACKLIST_PREFIX = "AUTH:TOKEN_BLACKLIST:";

    @Autowired
    private TokenEndpoint tokenEndpoint;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Oauth2登录认证
     */
    @ApiOperation("登录认证，获取token")
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public Result<Oauth2TokenDTO> postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenDTO oauth2TokenDto = Oauth2TokenDTO.builder()
                .token(Objects.requireNonNull(oAuth2AccessToken).getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .tokenHead("Bearer ").build();
        return Result.success(oauth2TokenDto);
    }

    @ApiOperation("退出登录，将当前 access token 加入黑名单")
    @DeleteMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (!StringUtils.hasText(token)) {
            return Result.failed("缺少 Bearer token");
        }

        OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
        if (accessToken == null) {
            return Result.failed("token 无效或已过期");
        }

        long ttlSeconds = resolveTtlSeconds(accessToken.getExpiration());
        redisTemplate.opsForValue().set(AUTH_TOKEN_BLACKLIST_PREFIX + token, 1, Duration.ofSeconds(ttlSeconds));
        return Result.success("退出登录成功");
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }

    private long resolveTtlSeconds(Date expiration) {
        if (expiration == null) {
            return 3600L;
        }
        long seconds = Duration.between(new Date().toInstant(), expiration.toInstant()).getSeconds();
        return Math.max(seconds, 1L);
    }

}
