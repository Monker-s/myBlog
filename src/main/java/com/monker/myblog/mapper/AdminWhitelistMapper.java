package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.AdminWhitelist;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：管理员白名单数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `admin_whitelist` 表，提供基础 CRUD 与按用户查询能力。
 */
@Mapper
public interface AdminWhitelistMapper extends BaseMapper<AdminWhitelist> {

    String BASE_COLUMNS = "id, user_id, granted_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM admin_whitelist WHERE id = #{id}")
    AdminWhitelist selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM admin_whitelist WHERE user_id = #{userId} LIMIT 1")
    AdminWhitelist selectByUserId(Long userId);

    @Select("SELECT " + BASE_COLUMNS + " FROM admin_whitelist ORDER BY id DESC")
    List<AdminWhitelist> selectAll();

    @Insert("""
            INSERT INTO admin_whitelist (user_id, granted_at)
            VALUES (#{userId}, #{grantedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminWhitelist adminWhitelist);

    @Update("""
            UPDATE admin_whitelist
            SET user_id = #{userId},
                granted_at = #{grantedAt}
            WHERE id = #{id}
            """)
    int updateById(AdminWhitelist adminWhitelist);

    @Delete("DELETE FROM admin_whitelist WHERE id = #{id}")
    int deleteById(Long id);
}
