package com.monker.myblog.vo;

import java.time.LocalDateTime;

/**
 * 文件用途：通知消息响应对象。
 * 关联接口：NotificationController.list、NotificationController.markRead。
 * 作用说明：向前端返回单条通知的展示内容与已读状态。
 *
 * @param id 通知主键
 * @param type 通知类型
 * @param postId 关联文章主键
 * @param postTitle 关联文章标题（可选）
 * @param title 通知标题
 * @param body 通知正文
 * @param isRead 是否已读（0-未读，1-已读）
 * @param readAt 已读时间
 * @param createdAt 创建时间
 */
public record NotificationResponse(
        Long id,
        String type,
        Long postId,
        String postTitle,
        String title,
        String body,
        Integer isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
