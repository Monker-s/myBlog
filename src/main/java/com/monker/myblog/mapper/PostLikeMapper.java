package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.PostLike;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：文章点赞数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `post_likes` 表，提供点赞记录的基础读写能力。
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    String BASE_COLUMNS = "id, post_id, user_id, created_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM post_likes WHERE id = #{id}")
    PostLike selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM post_likes ORDER BY id DESC")
    List<PostLike> selectAll();

    @Insert("""
            INSERT INTO post_likes (post_id, user_id, created_at)
            VALUES (#{postId}, #{userId}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostLike postLike);

    @Update("""
            UPDATE post_likes
            SET post_id = #{postId},
                user_id = #{userId},
                created_at = #{createdAt}
            WHERE id = #{id}
            """)
    int updateById(PostLike postLike);

    @Delete("DELETE FROM post_likes WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 函数用途：删除用户对文章的点赞记录。
     *
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 影响行数
     */
    @Delete("DELETE FROM post_likes WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 函数用途：统计某篇文章的真实点赞数。
     * 作用说明：以 `post_likes` 明细表为准，避免 `posts.like_count` 因历史异常产生漂移。
     *
     * @param postId 文章ID
     * @return 当前真实点赞数
     */
    @Select("SELECT COUNT(1) FROM post_likes WHERE post_id = #{postId}")
    int countByPostId(Long postId);

    /**
     * 函数用途：判断用户是否点赞过指定文章。
     * 作用说明：为点赞切换和点赞状态查询提供数据库真值判断。
     *
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 命中的点赞记录数
     */
    @Select("SELECT COUNT(1) FROM post_likes WHERE user_id = #{userId} AND post_id = #{postId}")
    int countByUserIdAndPostId(Long userId, Long postId);

    /**
     * 函数用途：查询点赞指定文章的所有用户ID。
     *
     * @param postId 文章ID
     * @return 点赞用户ID列表
     */
    @Select("SELECT user_id FROM post_likes WHERE post_id = #{postId}")
    List<Long> selectUserIdsByPostId(Long postId);
}
