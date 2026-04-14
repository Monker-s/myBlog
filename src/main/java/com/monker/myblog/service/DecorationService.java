package com.monker.myblog.service;

import com.monker.myblog.vo.DecorationConfigResponse;

/**
 * 文件用途：定义站点装饰配置业务能力。
 * 作用说明：统一提供当前生效的站点装饰配置查询能力。
 */
public interface DecorationService {

    /**
     * 函数用途：获取当前生效的站点装饰配置。
     *
     * @return 站点装饰配置
     */
    DecorationConfigResponse getActiveDecoration();
}
