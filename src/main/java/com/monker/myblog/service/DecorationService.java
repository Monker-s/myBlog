package com.monker.myblog.service;

import com.monker.myblog.dto.UpdateDecorationConfigDto;
import com.monker.myblog.vo.DecorationConfigResponse;

/**
 * 文件用途：定义站点装饰配置业务能力。
 * 作用说明：统一提供当前生效的站点装饰配置查询和更新能力。
 */
public interface DecorationService {

    /**
     * 函数用途：获取当前生效的站点装饰配置。
     *
     * @return 站点装饰配置
     */
    DecorationConfigResponse getActiveDecoration();

    /**
     * 函数用途：更新站点装饰配置。
     *
     * @param request 更新配置请求参数
     * @return 更新后的站点装饰配置
     */
    DecorationConfigResponse updateDecorationConfig(UpdateDecorationConfigDto request);
}
