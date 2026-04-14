package com.monker.myblog.service.impl;

import com.monker.myblog.service.StatsService;
import com.monker.myblog.vo.UvTrendPointResponse;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 文件用途：统计业务实现类。
 * 作用说明：当前提供后台统计模块的骨架实现，后续会在这里接入 UV/PV 聚合表和去重明细表计算逻辑。
 */
@Service
public class StatsServiceImpl implements StatsService {

    /**
     * 函数用途：模拟查询最近的 UV/PV 趋势。
     *
     * @return UV/PV 趋势点集合
     */
    @Override
    public List<UvTrendPointResponse> getRecentUvTrends() {
        return List.of();
    }
}
