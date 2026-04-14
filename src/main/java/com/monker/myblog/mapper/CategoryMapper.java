package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：文章分类数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `categories` 表，提供基础 CRUD 能力。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    String BASE_COLUMNS = "id, name, slug, parent_id, sort_order, description, created_at, updated_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM categories WHERE id = #{id}")
    Category selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM categories ORDER BY sort_order ASC, id ASC")
    List<Category> selectAll();

    @Insert("""
            INSERT INTO categories (name, slug, parent_id, sort_order, description, created_at, updated_at)
            VALUES (#{name}, #{slug}, #{parentId}, #{sortOrder}, #{description}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("""
            UPDATE categories
            SET name = #{name},
                slug = #{slug},
                parent_id = #{parentId},
                sort_order = #{sortOrder},
                description = #{description},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateById(Category category);

    @Delete("DELETE FROM categories WHERE id = #{id}")
    int deleteById(Long id);
}
