package com.vtmer.microteachingmessage.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Hung
 * @date 2022/7/27 1:09
 */
@Component
public class SnowFlakeUtil {
    @Bean
    public Snowflake snowflake() {
        return IdUtil.createSnowflake(1L, 1L);
    }
}
