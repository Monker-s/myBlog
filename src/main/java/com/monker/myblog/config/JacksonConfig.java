package com.monker.myblog.config;

import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件用途：定义 Jackson 序列化配置。
 * 作用说明：统一后端对外输出的时间时区，避免接口时间字段在不同时区下表现不一致。
 */
@Configuration
public class JacksonConfig {

    /**
     * 函数用途：将全局 JSON 时间序列化时区固定为 UTC。
     *
     * @return Jackson 自定义配置器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.timeZone(TimeZone.getTimeZone("UTC"));
    }
}
