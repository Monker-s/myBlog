package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.Post;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：文章主表数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `posts` 表，提供文章基础 CRUD 与逻辑删除过滤查询。
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    String BASE_COLUMNS = """
            id, title, slug, summary, cover_url, content_html, toc_json,
            author_id, category_id, status, is_pinned AS pinned, pinned_at,
            published_at, like_count, view_count, comment_count, deleted_at,
            created_at, updated_at
            """;

    @Select("SELECT " + BASE_COLUMNS + " FROM posts WHERE id = #{id}")
    Post selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM posts ORDER BY updated_at DESC, id DESC")
    List<Post> selectAll();

    @Select("SELECT " + BASE_COLUMNS + " FROM posts WHERE id = #{id} AND deleted_at IS NULL")
    Post selectByIdAndDeletedAtIsNull(Long id);

    /**
     * 函数用途：根据slug查询文章。
     *
     * @param slug 文章slug
     * @return 文章实体
     */
    @Select("SELECT " + BASE_COLUMNS + " FROM posts WHERE slug = #{slug} LIMIT 1")
    Post selectBySlug(String slug);

    /**
     * 函数用途：根据条件查询文章列表（用于分页）。
     * 注意：该方法使用 XML 配置动态 SQL，位于 resources/mapper/PostMapper.xml
     *
     * @param keyword 关键字搜索
     * @param categoryId 分类ID
     * @param status 文章状态
     * @param pinned 是否只查询置顶文章
     * @return 文章列表
     */
    List<Post> selectByCondition(String keyword, Long categoryId, Integer status, Boolean pinned);

    /**
     * 函数用途：管理后台根据条件查询文章列表（用于分页）。
     * 注意：该方法使用 XML 配置动态 SQL，位于 resources/mapper/PostMapper.xml
     *
     * @param keyword 关键字搜索
     * @param status 文章状态
     * @param categorySlug 分类slug
     * @param authorId 作者ID
     * @param orderBy 排序字段和方向
     * @return 文章列表
     */
    List<Post> selectAdminList(String keyword, Integer status, String categorySlug, Long authorId, String orderBy);

    @Insert("""
            INSERT INTO posts (
                title, slug, summary, cover_url, content_html, toc_json, author_id, category_id,
                status, is_pinned, pinned_at, published_at, like_count, view_count, comment_count,
                deleted_at, created_at, updated_at
            )
            VALUES (
                #{title}, #{slug}, #{summary}, #{coverUrl}, #{contentHtml}, #{tocJson}, #{authorId}, #{categoryId},
                #{status}, #{pinned}, #{pinnedAt}, #{publishedAt}, #{likeCount}, #{viewCount}, #{commentCount},
                #{deletedAt}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Post post);

    @Update("""
            UPDATE posts
            SET title = #{title},
                slug = #{slug},
                summary = #{summary},
                cover_url = #{coverUrl},
                content_html = #{contentHtml},
                toc_json = #{tocJson},
                author_id = #{authorId},
                category_id = #{categoryId},
                status = #{status},
                is_pinned = #{pinned},
                pinned_at = #{pinnedAt},
                published_at = #{publishedAt},
                like_count = #{likeCount},
                view_count = #{viewCount},
                comment_count = #{commentCount},
                deleted_at = #{deletedAt},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateById(Post post);

    @Delete("DELETE FROM posts WHERE id = #{id}")
    int deleteById(Long id);
}
