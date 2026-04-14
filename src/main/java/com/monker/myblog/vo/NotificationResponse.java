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
 * @param title 通知标题
 * @param body 通知正文
 * @param read 是否已读
 * @param readAt 已读时间
 * @param createdAt 创建时间
 */
public record NotificationResponse(
        Long id,
        String type,
        Long postId,
        String title,
        String body,
        Boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
