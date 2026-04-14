package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.PostTag;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文件用途：文章标签关联表数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `post_tags` 表，维护文章与标签的多对多关系。
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    String BASE_COLUMNS = "post_id, tag_id";

    @Select("SELECT " + BASE_COLUMNS + " FROM post_tags WHERE post_id = #{postId} AND tag_id = #{tagId}")
    PostTag selectById(@Param("postId") Long postId, @Param("tagId") Long tagId);

    @Select("SELECT " + BASE_COLUMNS + " FROM post_tags WHERE post_id = #{postId} ORDER BY tag_id ASC")
    List<PostTag> selectByPostId(Long postId);

    @Insert("""
            INSERT INTO post_tags (post_id, tag_id)
            VALUES (#{postId}, #{tagId})
            """)
    int insert(PostTag postTag);

    @Delete("DELETE FROM post_tags WHERE post_id = #{postId} AND tag_id = #{tagId}")
    int deleteById(@Param("postId") Long postId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM post_tags WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);
}
