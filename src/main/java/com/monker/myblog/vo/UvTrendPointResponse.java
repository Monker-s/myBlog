package com.monker.myblog.vo;

import java.time.LocalDate;

/**
 * 文件用途：访问趋势点响应对象。
 * 关联接口：AdminStatsController.trend。
 * 作用说明：按日期返回站点 UV 与 PV 统计结果。
 *
 * @param statDate 统计日期
 * @param uvCount 独立访客数
 * @param pvCount 页面浏览数
 */
public record UvTrendPointResponse(
        LocalDate statDate,
        Integer uvCount,
        Integer pvCount
) {
}
