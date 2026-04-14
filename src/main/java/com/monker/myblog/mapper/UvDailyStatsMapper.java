package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.UvDailyStats;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：站点 UV/PV 聚合统计数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `uv_daily_stats` 表，提供趋势查询与统计结果维护能力。
 */
@Mapper
public interface UvDailyStatsMapper extends BaseMapper<UvDailyStats> {

    String BASE_COLUMNS = "stat_date, uv_count, pv_count, created_at, updated_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM uv_daily_stats WHERE stat_date = #{statDate}")
    UvDailyStats selectById(LocalDate statDate);

    @Select("SELECT " + BASE_COLUMNS + " FROM uv_daily_stats ORDER BY stat_date DESC")
    List<UvDailyStats> selectAll();

    @Select("SELECT " + BASE_COLUMNS + " FROM uv_daily_stats ORDER BY stat_date DESC LIMIT 30")
    List<UvDailyStats> findTop30ByOrderByStatDateDesc();

    @Insert("""
            INSERT INTO uv_daily_stats (stat_date, uv_count, pv_count, created_at, updated_at)
            VALUES (#{statDate}, #{uvCount}, #{pvCount}, #{createdAt}, #{updatedAt})
            """)
    int insert(UvDailyStats uvDailyStats);

    @Update("""
            UPDATE uv_daily_stats
            SET uv_count = #{uvCount},
                pv_count = #{pvCount},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE stat_date = #{statDate}
            """)
    int updateById(UvDailyStats uvDailyStats);

    @Delete("DELETE FROM uv_daily_stats WHERE stat_date = #{statDate}")
    int deleteById(LocalDate statDate);
}
