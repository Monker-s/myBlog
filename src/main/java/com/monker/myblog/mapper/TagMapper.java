package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.Tag;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：标签数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `tags` 表，提供基础 CRUD 能力。
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    String BASE_COLUMNS = "id, name, slug, post_count, created_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM tags WHERE id = #{id}")
    Tag selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM tags ORDER BY id DESC")
    List<Tag> selectAll();

    /**
     * 函数用途：根据标签名称查询标签。
     *
     * @param name 标签名称
     * @return 标签实体
     */
    @Select("SELECT " + BASE_COLUMNS + " FROM tags WHERE name = #{name} LIMIT 1")
    Tag selectByName(String name);

    /**
     * 函数用途：根据文章ID查询标签名称列表。
     *
     * @param postId 文章ID
     * @return 标签名称列表
     */
    @Select("""
            SELECT t.name
            FROM tags t
            INNER JOIN post_tags pt ON t.id = pt.tag_id
            WHERE pt.post_id = #{postId}
            ORDER BY t.id ASC
            """)
    List<String> selectNamesByPostId(Long postId);

    /**
     * 函数用途：根据文章ID查询标签完整信息列表。
     *
     * @param postId 文章ID
     * @return 标签实体列表
     */
    @Select("""
            SELECT t.id, t.name, t.slug, t.post_count, t.created_at
            FROM tags t
            INNER JOIN post_tags pt ON t.id = pt.tag_id
            WHERE pt.post_id = #{postId}
            ORDER BY t.id ASC
            """)
    List<Tag> selectTagsByPostId(Long postId);

    @Insert("""
            INSERT INTO tags (name, slug, post_count, created_at)
            VALUES (#{name}, #{slug}, #{postCount}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Update("""
            UPDATE tags
            SET name = #{name},
                slug = #{slug},
                post_count = #{postCount},
                created_at = #{createdAt}
            WHERE id = #{id}
            """)
    int updateById(Tag tag);

    @Delete("DELETE FROM tags WHERE id = #{id}")
    int deleteById(Long id);
}
