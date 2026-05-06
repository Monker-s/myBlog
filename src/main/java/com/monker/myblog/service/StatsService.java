package com.monker.myblog.service;

import com.monker.myblog.vo.DataStatusResponse;
import com.monker.myblog.vo.UvTrendPointResponse;
import java.util.List;

/**
 * 文件用途：定义统计分析业务能力。
 * 作用说明：统一对外提供 UV/PV 趋势等统计类查询结果。
 */
public interface StatsService {

    /**
     * 函数用途：获取最近一段时间的 UV/PV 趋势。
     *
     * @return UV/PV 趋势点集合
     */
    List<UvTrendPointResponse> getRecentUvTrends();

    /**
     * 函数用途：获取文章统计数据。
     *
     * @return 文章统计响应对象
     */
    DataStatusResponse getPostStats();

    /**
     * 函数用途：获取用户统计数据。
     *
     * @return 用户统计响应对象
     */
    DataStatusResponse getUserStats();
}
