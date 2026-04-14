package com.monker.myblog.common;

/**
 * 文件用途：定义通知类型枚举。
 * 作用说明：供通知实体、通知服务和前端展示逻辑统一识别通知来源。
 */
public enum NotificationType {

    /** 文章内容被更新时触发的通知。 */
    POST_UPDATED,
    /** 评论收到回复时触发的通知。 */
    COMMENT_REPLY,
    /** 系统级通知。 */
    SYSTEM
}
