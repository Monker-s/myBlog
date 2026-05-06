package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.SiteDecoration;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：站点装扮配置数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `site_decorations` 表，提供装扮配置查询与维护能力。
 */
@Mapper
public interface SiteDecorationMapper extends BaseMapper<SiteDecoration> {

    String BASE_COLUMNS = """
            id, config_json, is_active AS active, version_tag, banner_image, banner_sub, banner_tag, hero_image, hero_eyebrow, created_at, updated_at
            """;

    @Select("SELECT " + BASE_COLUMNS + " FROM site_decorations WHERE id = #{id}")
    SiteDecoration selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM site_decorations ORDER BY id DESC")
    List<SiteDecoration> selectAll();

    @Select("""
            SELECT id, config_json, is_active AS active, version_tag, banner_image, banner_sub, banner_tag, hero_image, hero_eyebrow, created_at, updated_at
            FROM site_decorations
            WHERE is_active = 1
            ORDER BY id DESC
            LIMIT 1
            """)
    SiteDecoration findFirstByActiveTrueOrderByIdDesc();

    @Insert("""
            INSERT INTO site_decorations (config_json, is_active, version_tag, banner_image, banner_sub, banner_tag, hero_image, hero_eyebrow, created_at, updated_at)
            VALUES (#{configJson}, #{active}, #{versionTag}, #{bannerImage}, #{bannerSub}, #{bannerTag}, #{heroImage}, #{heroEyebrow}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteDecoration siteDecoration);

    @Update("""
            UPDATE site_decorations
            SET config_json = #{configJson},
                is_active = #{active},
                version_tag = #{versionTag},
                banner_image = #{bannerImage},
                banner_sub = #{bannerSub},
                banner_tag = #{bannerTag},
                hero_image = #{heroImage},
                hero_eyebrow = #{heroEyebrow},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateById(SiteDecoration siteDecoration);

    @Delete("DELETE FROM site_decorations WHERE id = #{id}")
    int deleteById(Long id);
}
