package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：用户主表数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `users` 表，提供认证流程需要的用户查询与持久化能力。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    String BASE_COLUMNS = "id, username, email, password_hash, icon, role, status, last_login_at, created_at, updated_at";

    @Select("SELECT " + BASE_COLUMNS + " FROM users WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM users ORDER BY id DESC")
    List<User> selectAll();

    @Select("SELECT " + BASE_COLUMNS + " FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(String username);

    @Select("SELECT " + BASE_COLUMNS + " FROM users WHERE email = #{email} LIMIT 1")
    User findByEmail(String email);

    @Select("""
            SELECT id, username, email, password_hash, icon, role, status, last_login_at, created_at, updated_at
            FROM users
            WHERE username = #{identifier} OR email = #{identifier}
            LIMIT 1
            """)
    User findByUsernameOrEmail(String identifier);

    @Insert("""
            INSERT INTO users (username, email, password_hash, icon, role, status, last_login_at, created_at, updated_at)
            VALUES (#{username}, #{email}, #{passwordHash}, #{icon}, #{role}, #{status}, #{lastLoginAt}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("""
            UPDATE users
            SET username = #{username},
                email = #{email},
                password_hash = #{passwordHash},
                icon = #{icon},
                role = #{role},
                status = #{status},
                last_login_at = #{lastLoginAt},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateById(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 函数用途：查询所有正常状态的用户ID。
     *
     * @return 用户ID列表
     */
    @Select("SELECT id FROM users WHERE status = 1")
    List<Long> selectAllActiveUserIds();
}
