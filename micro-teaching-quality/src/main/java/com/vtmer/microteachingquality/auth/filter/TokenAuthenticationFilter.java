package com.vtmer.microteachingquality.auth.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.vtmer.microteachingquality.auth.config.IgnoreUrlsConfig;
import com.vtmer.microteachingquality.auth.constant.RedisConstant;
import com.vtmer.microteachingquality.model.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 拦截发送到api的请求头的token并解析
 *
 * @author Hung
 * @date 2021/11/6 15:57
 **/
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Autowired
    TokenStore tokenStore;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String uri = httpServletRequest.getRequestURI();
        // 白名单路径访问时需要移除JWT请求头
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri)) {
                // 检测到白名单并进行处理
                String newUri = uri.replaceFirst("^/teaching", "");
                HttpServletRequest wrappedRequest = new PathRewriteRequestWrapper(httpServletRequest, newUri);
                filterChain.doFilter(wrappedRequest, httpServletResponse);
            }
        }

        // 解token
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (StrUtil.isEmpty(authHeader)|| !authHeader.startsWith("Bearer ")) {
            error(httpServletResponse);
            return;
        }
        String jwtToken = authHeader.substring(7);
        OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(jwtToken);
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(oAuth2AccessToken);
        Authentication userAuthentication = oAuth2Authentication.getUserAuthentication();
        //取出用户身份信息
        String principal = userAuthentication.getName();
        //取出用户权限
        String[] authorities = userAuthentication.getAuthorities().stream()
                .map((Function<GrantedAuthority, String>) GrantedAuthority::getAuthority)
                .collect(Collectors.toList()).toArray(new String[]{});

        // 检验权限
        Object obj = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, uri);
        StringBuilder path = new StringBuilder("/teaching/");
        String[] split = uri.split("\\/");
        for (int i = 0; i < split.length; i++) {
            if ("teaching".equals(split[i])) {
                path.append(split[i + 1]);
            }
        }

        Object objStartWith = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, path.toString());
        List<String> authoritiesNeed = Convert.toList(String.class, obj);
        authoritiesNeed.addAll(Convert.toList(String.class, objStartWith));
        Set<String> hash = new HashSet<>(authoritiesNeed);
        for (String authority : authorities) {
            if (hash.contains(authority)) {
                hash.clear();
            }
        }
        if (!hash.isEmpty()) {
            error(httpServletResponse);
            return;
        }

        User userDTO = JSON.parseObject(principal, User.class);

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(userDTO, null, AuthorityUtils.createAuthorityList(authorities));
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String newUri = uri.replaceFirst("^/teaching", "");
        HttpServletRequest wrappedRequest = new PathRewriteRequestWrapper(httpServletRequest, newUri);
        filterChain.doFilter(wrappedRequest, httpServletResponse);
    }

    private void error(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(401);
        httpServletResponse.getWriter().write("{\"error\": \"Unauthorized\"}");
    }

}
