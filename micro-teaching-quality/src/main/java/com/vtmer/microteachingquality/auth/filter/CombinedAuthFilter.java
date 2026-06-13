package com.vtmer.microteachingquality.auth.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vtmer.microteachingquality.auth.config.IgnoreUrlsConfig;
import com.vtmer.microteachingquality.auth.constant.AuthConstant;
import com.vtmer.microteachingquality.auth.constant.RedisConstant;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 合并后的认证与授权过滤器
 */
@Slf4j
public class CombinedAuthFilter extends OncePerRequestFilter {

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TokenStore tokenStore;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        HttpServletRequest modifiedRequest = request;

        // 1. 白名单处理：移除Authorization头
        if (isIgnoreUrl(requestUri)) {
            modifiedRequest = new CustomRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equals(name)) {
                        return "";
                    }
                    return super.getHeader(name);
                }
            };
        }

        // 2. 解析Authorization头中的Bearer token
        String bearerToken = modifiedRequest.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            try {
                String realToken = bearerToken.replace("Bearer ", "");
                OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(realToken);
                OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(oAuth2AccessToken);
                Authentication userAuth = oAuth2Authentication.getUserAuthentication();

                if (userAuth != null) {
                    String principal = userAuth.getName();
                    List<String> authorities = userAuth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    OAuth2Request oAuth2Request = oAuth2Authentication.getOAuth2Request();
                    Map<String, String> requestParams = oAuth2Request.getRequestParameters();
                    Map<String, Object> jsonToken = new HashMap<>(requestParams);
                    jsonToken.put("principal", principal);
                    jsonToken.put("authorities", authorities);

                    CustomRequestWrapper requestWrapper = new CustomRequestWrapper(modifiedRequest);
                    requestWrapper.setHeader("json_token",
                            EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(jsonToken)));
                    modifiedRequest = requestWrapper;

                    SecurityContextHolder.getContext().setAuthentication(userAuth);
                }
            } catch (Exception e) {
                log.error("解析Bearer token失败", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Bearer token");
                return;
            }
        }

        // 3. 解析json_token头（处理下游服务传递的token）
        String jsonToken = modifiedRequest.getHeader("json_token");
        if (StrUtil.isNotBlank(jsonToken)) {
            try {
                String json = EncryptUtil.decodeUTF8StringBase64(jsonToken);
                JSONObject jsonObject = JSON.parseObject(json);
                User user = JSON.parseObject(jsonObject.getString("principal"), User.class);
                JSONArray authoritiesArray = jsonObject.getJSONArray("authorities");
                String[] authorities = authoritiesArray.toArray(new String[authoritiesArray.size()]);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        user, null, AuthorityUtils.createAuthorityList(authorities));
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(modifiedRequest));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                log.error("解析json_token失败", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid json token");
                return;
            }
        }

        // 4. 权限校验（非白名单路径）
        if (!isIgnoreUrl(requestUri)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未认证");
                return;
            }

            StringBuilder path = new StringBuilder("/teaching/");
            String[] split = requestUri.split("/");
            for (int i = 0; i < split.length; i++) {
                if ("teaching".equals(split[i]) && i + 1 < split.length) {
                    path.append(split[i + 1]);
                    break;
                }
            }

            Object obj = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, requestUri);
            Object objStartWith = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, path.toString());

            List<String> requiredAuthorities = Convert.toList(String.class, obj);
            requiredAuthorities.addAll(Convert.toList(String.class, objStartWith));
            requiredAuthorities = requiredAuthorities.stream()
                    .map(auth -> AuthConstant.AUTHORITY_PREFIX + auth)
                    .collect(Collectors.toList());

            boolean hasPermission = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(requiredAuthorities::contains);

            if (!hasPermission) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
                return;
            }
        }

        filterChain.doFilter(modifiedRequest, response);
    }

    private boolean isIgnoreUrl(String uri) {
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri)) {
                return true;
            }
        }
        return false;
    }

    private static class CustomRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
        private final Map<String, String> headers = new HashMap<>();

        public CustomRequestWrapper(HttpServletRequest request) {
            super(request);
            request.getHeaderNames().asIterator().forEachRemaining(name ->
                    headers.put(name, request.getHeader(name)));
        }

        @Override
        public String getHeader(String name) {
            return headers.getOrDefault(name, super.getHeader(name));
        }

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }
    }
}