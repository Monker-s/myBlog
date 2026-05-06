package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：评论数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `comments` 表，提供评论查询与写入能力。
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    String BASE_COLUMNS = "id, post_id, user_id, parent_id, depth, content, status, created_at, updated_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM comments WHERE id = #{id}")
    Comment selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM comments WHERE post_id = #{postId} ORDER BY created_at ASC")
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    @Insert("""
            INSERT INTO comments (post_id, user_id, parent_id, depth, content, status, created_at, updated_at)
            VALUES (#{postId}, #{userId}, #{parentId}, #{depth}, #{content}, #{status}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Update("""
            UPDATE comments
            SET post_id = #{postId},
                user_id = #{userId},
                parent_id = #{parentId},
                depth = #{depth},
                content = #{content},
                status = #{status},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateById(Comment comment);

    @Delete("DELETE FROM comments WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 函数用途：查询指定文章的所有评论用户ID（去重）。
     *
     * @param postId 文章ID
     * @return 评论用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM comments WHERE post_id = #{postId}")
    List<Long> selectUserIdsByPostId(Long postId);
}
