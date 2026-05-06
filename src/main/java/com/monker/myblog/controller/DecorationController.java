package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.dto.UpdateDecorationConfigDto;
import com.monker.myblog.service.DecorationService;
import com.monker.myblog.vo.DecorationConfigResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供站点装饰配置查询和更新接口。
 * 作用说明：向前端返回当前生效的站点动态配置，支持更新主题、颜色和 Banner 图片等配置。
 */
@RestController
@RequestMapping("/api/admin/config")
@Slf4j
public class DecorationController {

    @Autowired
    private DecorationService decorationService;


    /**
     * 函数用途：查询当前生效的站点装饰配置。
     *
     * @return 站点装饰配置
     */
    @GetMapping("/decoration")
    public Result<DecorationConfigResponse> getDecorationConfig() {
        log.info("获取站点装饰配置");
        return Result.success(decorationService.getActiveDecoration());
    }

    /**
     * 函数用途：更新站点装饰配置。
     *
     * @param request 更新配置请求参数（包含 theme、primaryColor、accentColor、bannerImage）
     * @return 更新后的站点装饰配置
     */
    @PutMapping("/decoration")
    public Result<DecorationConfigResponse> updateDecorationConfig(
            @Valid @RequestBody UpdateDecorationConfigDto request) {
        log.info("更新站点装饰配置: theme={}, bannerImage={}", request.theme(), request.bannerImage());
        DecorationConfigResponse response = decorationService.updateDecorationConfig(request);
        return Result.success("配置更新成功", response);
    }
}
