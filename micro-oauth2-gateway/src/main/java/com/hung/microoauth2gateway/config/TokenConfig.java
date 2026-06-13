package com.hung.microoauth2gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;

/**
 * @author Hung
 * @date 2021/11/5 21:28
 */
@Configuration
public class TokenConfig {

    @Value("${security.jwt.key-store-password}")
    private String keyStorePassword;

    @Value("${security.jwt.key-pair-alias}")
    private String keyPairAlias;

    @Value("${security.jwt.key-pair-password}")
    private String keyPairPassword;

    @Bean
    public TokenStore tokenStore() {
        //可以选择jdbcTokenStore将token存在数据库表oauth_access_token中
        //还可以选择RedisTokenStore存在redis中
        //JWT令牌存储方案 用于生成和检索令牌
        return new JwtTokenStore(accessTokenConverter());
    }

    /**
     * 用于将访问令牌转化成不同的格式
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setKeyPair(keyPair());
        return jwtAccessTokenConverter;
    }

    @Bean
    public KeyPair keyPair() {
        //从classpath下的证书中获取秘钥对
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), keyStorePassword.toCharArray());
        return keyStoreKeyFactory.getKeyPair(keyPairAlias, keyPairPassword.toCharArray());
    }
}
