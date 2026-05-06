package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monker.myblog.entity.Post;
import com.monker.myblog.entity.UvDailyStats;
import com.monker.myblog.entity.User;
import com.monker.myblog.mapper.PostMapper;
import com.monker.myblog.mapper.UvDailyStatsMapper;
import com.monker.myblog.mapper.UserMapper;
import com.monker.myblog.service.StatsService;
import com.monker.myblog.vo.DataStatusResponse;
import com.monker.myblog.vo.UvTrendPointResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件用途：统计业务实现类。
 * 作用说明：当前提供后台统计模块的骨架实现，后续会在这里接入 UV/PV 聚合表和去重明细表计算逻辑。
 */
@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UvDailyStatsMapper uvDailyStatsMapper;

    /**
     * 函数用途：查询最近 30 天的 UV/PV 趋势。
     *
     * @return UV/PV 趋势点集合
     */
    @Override
    public List<UvTrendPointResponse> getRecentUvTrends() {
        log.info("查询最近 30 天 UV/PV 趋势数据");
        
        // 从数据库查询最近 30 天的统计数据
        List<UvDailyStats> statsList = uvDailyStatsMapper.findTop30ByOrderByStatDateDesc();
        
        // 转换为响应对象
        List<UvTrendPointResponse> trends = statsList.stream()
                .map(stats -> new UvTrendPointResponse(
                        stats.getStatDate(),
                        stats.getUvCount(),
                        stats.getPvCount()
                ))
                .toList();
        
        log.info("UV/PV 趋势数据查询完成，共 {} 条记录", trends.size());
        return trends;
    }

    /**
     * 函数用途：获取文章统计数据。
     *
     * @return 文章统计响应对象
     */
    @Override
    public DataStatusResponse getPostStats() {
        // 1. 查询文章总数
        Long total = postMapper.selectCount(null);

        // 2. 查询今日新增文章数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<Post> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Post::getCreatedAt, todayStart);
        Long newToday = postMapper.selectCount(todayWrapper);

        // 3. 查询本周新增文章数（从本周一开始）
        LocalDate monday = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDateTime weekStart = LocalDateTime.of(monday, LocalTime.MIN);
        LambdaQueryWrapper<Post> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(Post::getCreatedAt, weekStart);
        Long newWeek = postMapper.selectCount(weekWrapper);

        // 4. 查询本月新增文章数（从本月1号开始）
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LambdaQueryWrapper<Post> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(Post::getCreatedAt, monthStart);
        Long newMonth = postMapper.selectCount(monthWrapper);

        log.info("文章统计数据: total={}, newToday={}, newWeek={}, newMonth={}", 
                total, newToday, newWeek, newMonth);

        return new DataStatusResponse(total, newToday, newWeek, newMonth);
    }

    /**
     * 函数用途：获取用户统计数据。
     *
     * @return 用户统计响应对象
     */
    @Override
    public DataStatusResponse getUserStats() {
        // 1. 查询用户总数
        Long total = userMapper.selectCount(null);

        // 2. 查询今日新增用户数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<User> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(User::getCreatedAt, todayStart);
        Long newToday = userMapper.selectCount(todayWrapper);

        // 3. 查询本周新增用户数（从本周一开始）
        LocalDate monday = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDateTime weekStart = LocalDateTime.of(monday, LocalTime.MIN);
        LambdaQueryWrapper<User> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(User::getCreatedAt, weekStart);
        Long newWeek = userMapper.selectCount(weekWrapper);

        // 4. 查询本月新增用户数（从本月1号开始）
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LambdaQueryWrapper<User> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(User::getCreatedAt, monthStart);
        Long newMonth = userMapper.selectCount(monthWrapper);

        log.info("用户统计数据: total={}, newToday={}, newWeek={}, newMonth={}", 
                total, newToday, newWeek, newMonth);

        return new DataStatusResponse(total, newToday, newWeek, newMonth);
    }
}
