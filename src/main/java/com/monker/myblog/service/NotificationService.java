package com.monker.myblog.service;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.MarkNotificationReadDto;
import com.monker.myblog.vo.NotificationResponse;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文件用途：定义通知中心业务能力。
 * 作用说明：统一提供通知列表查询、未读查询、已读更新和 SSE 推送能力。
 */
public interface NotificationService {

    /**
     * 函数用途：分页查询通知列表。
     *
     * @param read 是否已读筛选条件
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果
     */
    PageResponse<NotificationResponse> listNotifications(Boolean read, int page, int size);

    /**
     * 函数用途：查询未读通知。
     *
     * @return 未读通知集合
     */
    List<NotificationResponse> listUnreadNotifications();

    /**
     * 函数用途：批量将通知标记为已读。
     *
     * @param request 已读参数
     */
    void markRead(MarkNotificationReadDto request);

    /**
     * 函数用途：建立通知 SSE 推送通道。
     *
     * @return SSE 发射器
     */
    SseEmitter openStream();
}
