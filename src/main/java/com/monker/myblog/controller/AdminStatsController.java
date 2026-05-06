package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.config.UvStatsSyncTask;
import com.monker.myblog.service.StatsService;
import com.monker.myblog.vo.DataStatusResponse;
import com.monker.myblog.vo.UvTrendPointResponse;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供后台统计接口。
 * 作用说明：当前主要暴露 UV/PV 趋势查询能力，供管理后台看板调用。
 */
@RestController
@Slf4j
@RequestMapping("/api/admin/status")
public class AdminStatsController {

    @Autowired
    private StatsService statsService;

    @Autowired
    private UvStatsSyncTask uvStatsSyncTask;


    /**
     * 函数用途：查询最近一段时间的 UV/PV 趋势。
     *
     * @return UV/PV 趋势点集合
     */
    @GetMapping("/uv")
    public Result<List<UvTrendPointResponse>> getRecentUvStats() {
        log.info("获取UV数据");
        return Result.success(statsService.getRecentUvTrends());
    }

    /**
     * 函数用途：获取文章统计数据。
     *
     * @return 文章统计响应对象
     */
    @GetMapping("/posts")
    public Result<DataStatusResponse> getPostStats() {
        log.info("获取文章数据");
        return Result.success(statsService.getPostStats());
    }

    /**
     * 函数用途：获取用户统计数据。
     *
     * @return 用户统计响应对象
     */
    @GetMapping("/users")
    public Result<DataStatusResponse> getUserStats() {
        log.info("获取用户数据");
        return Result.success(statsService.getUserStats());
    }

    /**
     * 函数用途：手动触发 UV/PV 数据同步（测试用）。
     *
     * @return 同步结果
     */
    @PostMapping("/sync-uv")
    public Result<String> syncUvStats() {
        log.info("管理员手动触发 UV/PV 数据同步");
        uvStatsSyncTask.syncTodayStats();
        return Result.success("UV/PV 数据同步成功");
    }
}
