package com.monker.myblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 文件用途：后端项目启动入口。
 * 作用说明：负责启动 Spring Boot 容器，并扫描 application.yml 中定义的配置属性类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.monker.myblog.mapper")
@EnableScheduling  // 启用定时任务
public class MyBlogApplication {

    /**
     * 函数用途：启动整个后端应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MyBlogApplication.class, args);
    }
}
