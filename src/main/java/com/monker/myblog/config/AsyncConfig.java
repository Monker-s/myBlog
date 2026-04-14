package com.monker.myblog.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 文件用途：定义项目异步执行配置。
 * 作用说明：集中管理异步线程池，供通知投递、统计计算等后台任务复用。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 函数用途：注册应用级异步线程池。
     *
     * @return 可供 Spring 注入的异步执行器
     */
    @Bean(name = "applicationTaskExecutor")
    public Executor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("myblog-async-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }
}
