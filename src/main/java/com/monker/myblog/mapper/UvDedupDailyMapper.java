package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.UvDedupDaily;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：站点访问去重明细数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `uv_dedup_daily` 表，记录站点访客去重明细。
 */
@Mapper
public interface UvDedupDailyMapper extends BaseMapper<UvDedupDaily> {

    String BASE_COLUMNS = "id, stat_date, ip_prefix_hash, ua_hash, visitor_hash, created_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM uv_dedup_daily WHERE id = #{id}")
    UvDedupDaily selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM uv_dedup_daily ORDER BY id DESC")
    List<UvDedupDaily> selectAll();

    @Insert("""
            INSERT INTO uv_dedup_daily (stat_date, ip_prefix_hash, ua_hash, visitor_hash, created_at)
            VALUES (#{statDate}, #{ipPrefixHash}, #{uaHash}, #{visitorHash}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UvDedupDaily uvDedupDaily);

    @Update("""
            UPDATE uv_dedup_daily
            SET stat_date = #{statDate},
                ip_prefix_hash = #{ipPrefixHash},
                ua_hash = #{uaHash},
                visitor_hash = #{visitorHash},
                created_at = #{createdAt}
            WHERE id = #{id}
            """)
    int updateById(UvDedupDaily uvDedupDaily);

    @Delete("DELETE FROM uv_dedup_daily WHERE id = #{id}")
    int deleteById(Long id);
}
