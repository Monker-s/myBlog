package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：通知中心实体。
 * 数据库表：notifications。
 * 对应接口：NotificationController。
 * 对应服务与访问层：NotificationService、NotificationServiceImpl、NotificationMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notifications")
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private Long postId;
    private String contentHash;
    private String title;
    private String body;
    private Integer isRead;
    private LocalDateTime readAt;
    private String payloadJson;
    private LocalDateTime createdAt;
}



