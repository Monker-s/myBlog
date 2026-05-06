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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供通知列表、已读更新接口。
 * 作用说明：统一承接通知中心请求，并调用 {@link NotificationService} 返回通知相关结果。
 * 注意：实时推送功能已迁移到 WebSocket，前端需连接 ws://host/ws/notifications/{token}
 */
@Validated
@RestController
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;



    /**
     * 函数用途：分页查询通知列表并返回未读数量。
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果（包含所有通知和未读数量）
     */
    @GetMapping
    public Result<PageResponse<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量最少为1")
            @Max(value = 100, message = "每页数量最多为100") int size
    ) {
        log.info("获取通知数据");
        return Result.success(notificationService.listNotifications(page, size));
    }

    /**
     * 函数用途：获取未读的系统公告（用于登录后弹窗显示）。
     * 作用说明：返回最新的未读 SYSTEM 类型通知，并自动标记为已读。
     *
     * @return 系统公告列表（通常只有最新的一条）
     */
    @GetMapping("/system-announcement")
    public Result<List<NotificationResponse>> getSystemAnnouncement() {
        log.info("获取未读系统公告");
        return Result.success(notificationService.getUnreadSystemAnnouncements());
    }

    /**
     * 函数用途：批量将通知标记为已读。
     *
     * @param request 已读参数
     * @return 统一成功响应
     */
    @PutMapping("/read")
    public Result<Void> markRead(@Valid @RequestBody MarkNotificationReadDto request) {
        log.info("批量将通知标记为已读:{}",request);
        notificationService.markRead(request);
        return Result.success("通知已标记为已读", null);
    }
}
