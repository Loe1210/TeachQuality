package com.vtmer.microteachingquality.common.config;

import com.vtmer.microteachingquality.common.interceptor.SchoolAccountInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * 提供跨域支持
 */

public class CorsConfig extends WebMvcConfigurationSupport {

    @Value("#{'${app.cors.allowed-origins:http://localhost:8080}'.split(',')}")
    private List<String> allowedOrigins;

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 开启发送Cookie和HTTP认证信息
        corsConfiguration.setAllowCredentials(true);
        allowedOrigins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(corsConfiguration::addAllowedOrigin);
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }


    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowCredentials(true)
                .allowedMethods("*")
                .maxAge(3600);
    }

    /**
     * 跨域配置后swagger-ui可能不能访问，需要增加如下配置
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.
                addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
        registry
                .addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/")
                .resourceChain(false);
        registry
                .addResourceHandler("/doc.html#/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .resourceChain(false);
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui")
                .setViewName("forward:/swagger-ui/index.html");
    }


    /**
     * 添加拦截器，对请求课程自评报告相关的接口进行身份校验
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SchoolAccountInterceptor()).addPathPatterns("/coursereport/schoolAccount/**");
    }
}
