package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.service.StatsService;
import com.monker.myblog.vo.UvTrendPointResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供后台统计接口。
 * 作用说明：当前主要暴露 UV/PV 趋势查询能力，供管理后台看板调用。
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final StatsService statsService;

    /**
     * 函数用途：注入统计服务。
     *
     * @param statsService 统计服务
     */
    public AdminStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 函数用途：查询最近一段时间的 UV/PV 趋势。
     *
     * @return UV/PV 趋势点集合
     */
    @GetMapping("/uv")
    public Result<List<UvTrendPointResponse>> getRecentUvStats() {
        return Result.success(statsService.getRecentUvTrends());
    }
}
