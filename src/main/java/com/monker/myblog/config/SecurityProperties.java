package com.monker.myblog.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件用途：绑定安全相关自定义配置。
 * 作用说明：读取 application.yml 中的 `app.security` 配置，统一管理允许跨域访问的来源列表。
 */
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 函数用途：返回允许访问当前后端的前端来源列表。
     *
     * @return 允许的来源列表
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * 函数用途：设置允许访问当前后端的前端来源列表。
     *
     * @param allowedOrigins 允许的来源列表
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
