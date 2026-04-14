package com.monker.myblog.service.impl;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.MarkNotificationReadDto;
import com.monker.myblog.service.NotificationService;
import com.monker.myblog.vo.NotificationResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文件用途：通知中心业务实现类。
 * 作用说明：当前提供通知模块的骨架实现，后续会在这里接入通知表、SSE 推送和已读写入逻辑。
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    /**
     * 函数用途：模拟分页查询通知列表。
     *
     * @param read 是否已读筛选条件
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果
     */
    @Override
    public PageResponse<NotificationResponse> listNotifications(Boolean read, int page, int size) {
        return PageResponse.empty(page, size);
    }

    /**
     * 函数用途：模拟查询未读通知列表。
     *
     * @return 未读通知集合
     */
    @Override
    public List<NotificationResponse> listUnreadNotifications() {
        return List.of();
    }

    /**
     * 函数用途：模拟批量标记通知为已读。
     *
     * @param request 已读参数
     */
    @Override
    public void markRead(MarkNotificationReadDto request) {
        // 已读状态持久化将在真实实现中接入 mapper。
    }

    /**
     * 函数用途：建立通知 SSE 推送通道。
     *
     * @return SSE 发射器
     */
    @Override
    public SseEmitter openStream() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(new NotificationResponse(
                            0L,
                            "SYSTEM",
                            null,
                            "通知通道已建立",
                            "当前为结构骨架占位事件",
                            false,
                            null,
                            LocalDateTime.now()
                    )));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
        return emitter;
    }
}
