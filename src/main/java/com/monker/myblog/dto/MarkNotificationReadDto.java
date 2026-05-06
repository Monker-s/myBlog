package com.monker.myblog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 文件用途：通知已读请求参数。
 * 关联接口：NotificationController.markRead。
 * 作用说明：批量接收需要标记为已读状态的通知主键列表。
 *
 * @param notificationIds 待标记为已读的通知 ID 集合
 */
public record MarkNotificationReadDto(
        @JsonProperty("notificationIds")
        @NotEmpty(message = "至少传入一条通知 ID")
        List<Long> notificationIds
) {
}
