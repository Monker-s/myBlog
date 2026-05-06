package com.monker.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monker.myblog.entity.Notification;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文件用途：站内通知数据访问接口。
 * 作用说明：使用 MyBatis-Plus 访问 `notifications` 表，提供通知查询与状态更新能力。
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    String BASE_COLUMNS = """
            id, user_id, type, post_id, content_hash, title, body,
            is_read AS isRead, read_at, payload_json, created_at
            """;

    @Select("SELECT " + BASE_COLUMNS + " FROM notifications WHERE id = #{id}")
    Notification selectById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM notifications ORDER BY created_at DESC, id DESC")
    List<Notification> selectAll();

    @Select("""
            SELECT id, user_id, type, post_id, content_hash, title, body,
                   is_read AS isRead, read_at, payload_json, created_at
            FROM notifications
            WHERE user_id = #{userId} AND is_read = 0
            ORDER BY created_at DESC, id DESC
            """)
    List<Notification> findAllByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    @Insert("""
            INSERT INTO notifications (
                user_id, type, post_id, content_hash, title, body, is_read, read_at, payload_json, created_at
            )
            VALUES (
                #{userId}, #{type}, #{postId}, #{contentHash}, #{title}, #{body},
                #{isRead}, #{readAt}, #{payloadJson}, #{createdAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Update("""
            UPDATE notifications
            SET user_id = #{userId},
                type = #{type},
                post_id = #{postId},
                content_hash = #{contentHash},
                title = #{title},
                body = #{body},
                is_read = #{isRead},
                read_at = #{readAt},
                payload_json = #{payloadJson},
                created_at = #{createdAt}
            WHERE id = #{id}
            """)
    int updateById(Notification notification);

    @Delete("DELETE FROM notifications WHERE id = #{id}")
    int deleteById(Long id);
}
