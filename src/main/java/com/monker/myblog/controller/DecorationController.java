package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.service.DecorationService;
import com.monker.myblog.vo.DecorationConfigResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供站点装饰配置查询接口。
 * 作用说明：向前端返回当前生效的站点动态配置，供页面加载主题和装饰内容使用。
 */
@RestController
@RequestMapping("/api/config")
public class DecorationController {

    private final DecorationService decorationService;

    /**
     * 函数用途：注入站点装饰服务。
     *
     * @param decorationService 站点装饰服务
     */
    public DecorationController(DecorationService decorationService) {
        this.decorationService = decorationService;
    }

    /**
     * 函数用途：查询当前生效的站点装饰配置。
     *
     * @return 站点装饰配置
     */
    @GetMapping("/decoration")
    public Result<DecorationConfigResponse> getDecorationConfig() {
        return Result.success(decorationService.getActiveDecoration());
    }
}
