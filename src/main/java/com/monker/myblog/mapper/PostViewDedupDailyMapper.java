package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.PostViewDedupDaily;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：文章浏览去重明细数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `post_view_dedup_daily` 表，记录文章浏览去重明细。
 */
@Mapper
public interface PostViewDedupDailyMapper extends BaseMapper<PostViewDedupDaily> {

    String BASE_COLUMNS = "id, post_id, dedup_date, dedup_key_hash, created_at, last_viewed_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM post_view_dedup_daily WHERE id = #{id}")
    PostViewDedupDaily selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM post_view_dedup_daily ORDER BY id DESC")
    List<PostViewDedupDaily> selectAll();

    @Insert("""
            INSERT INTO post_view_dedup_daily (post_id, dedup_date, dedup_key_hash, created_at, last_viewed_at)
            VALUES (#{postId}, #{dedupDate}, #{dedupKeyHash}, #{createdAt}, #{lastViewedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostViewDedupDaily postViewDedupDaily);

    @Update("""
            UPDATE post_view_dedup_daily
            SET post_id = #{postId},
                dedup_date = #{dedupDate},
                dedup_key_hash = #{dedupKeyHash},
                created_at = #{createdAt},
                last_viewed_at = #{lastViewedAt}
            WHERE id = #{id}
            """)
    int updateById(PostViewDedupDaily postViewDedupDaily);

    @Delete("DELETE FROM post_view_dedup_daily WHERE id = #{id}")
    int deleteById(Long id);
}
