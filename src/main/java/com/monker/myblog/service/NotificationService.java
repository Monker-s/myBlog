package com.monker.myblog.service;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.BroadcastNotificationDto;
import com.monker.myblog.dto.MarkNotificationReadDto;
import com.monker.myblog.vo.NotificationResponse;
import java.util.List;

/**
 * 文件用途：定义通知中心业务能力。
 * 作用说明：统一提供通知列表查询、已读更新能力。
 * 注意：实时推送功能已迁移到 WebSocket + Redis Pub/Sub
 */
public interface NotificationService {

    /**
     * 函数用途：分页查询通知列表并返回未读数量。
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果（包含所有通知和未读数量）
     */
    PageResponse<NotificationResponse> listNotifications(int page, int size);

    /**
     * 函数用途：批量将通知标记为已读。
     *
     * @param request 已读参数
     */
    void markRead(MarkNotificationReadDto request);

    /**
     * 函数用途：获取未读的系统公告。
     * 作用说明：返回最新的未读 SYSTEM 类型通知，并自动标记为已读。
     *
     * @return 系统公告列表
     */
    java.util.List<NotificationResponse> getUnreadSystemAnnouncements();

    /**
     * 函数用途：管理员群发通知。
     *
     * @param request 群发通知参数
     */
    void broadcastNotification(BroadcastNotificationDto request);

    /**
     * 函数用途：管理员删除通知。
     *
     * @param id 通知ID
     */
    void deleteNotification(Long id);
}
