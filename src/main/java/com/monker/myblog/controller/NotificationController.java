package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.MarkNotificationReadDto;
import com.monker.myblog.service.NotificationService;
import com.monker.myblog.vo.NotificationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文件用途：提供通知列表、未读查询、已读更新和 SSE 推送接口。
 * 作用说明：统一承接通知中心请求，并调用 {@link NotificationService} 返回通知相关结果。
 */
@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 函数用途：注入通知服务。
     *
     * @param notificationService 通知服务
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 函数用途：分页查询通知列表。
     *
     * @param read 是否已读筛选条件
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果
     */
    @GetMapping
    public Result<PageResponse<NotificationResponse>> listNotifications(
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量最少为1")
            @Max(value = 100, message = "每页数量最多为100") int size
    ) {
        return Result.success(notificationService.listNotifications(read, page, size));
    }

    /**
     * 函数用途：查询未读通知列表。
     *
     * @return 未读通知集合
     */
    @GetMapping("/unread")
    public Result<List<NotificationResponse>> listUnreadNotifications() {
        return Result.success(notificationService.listUnreadNotifications());
    }

    /**
     * 函数用途：批量将通知标记为已读。
     *
     * @param request 已读参数
     * @return 统一成功响应
     */
    @PutMapping("/read")
    public Result<Void> markRead(@Valid @RequestBody MarkNotificationReadDto request) {
        notificationService.markRead(request);
        return Result.success("通知已标记为已读", null);
    }

    /**
     * 函数用途：建立通知 SSE 长连接。
     *
     * @return SSE 推送通道
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return notificationService.openStream();
    }
}
