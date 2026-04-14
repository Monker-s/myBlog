package com.monker.myblog.vo;

import java.time.LocalDateTime;

/**
 * 文件用途：文章浏览上报响应数据对象。
 * 关联接口：PostController.recordView。
 * 作用说明：返回浏览记录的处理结果，包含去重标识和请求追踪信息。
 *
 * @param accepted 请求是否被接受（浏览记录是否成功）
 * @param deduplicated 是否进行了去重处理（防刷机制）
 * @param requestId 请求唯一标识，用于追踪和调试
 * @param timestamp 服务器响应时间戳
 */
public record ViewReportResultData(
        Boolean accepted,
        Boolean deduplicated,
        String requestId,
        LocalDateTime timestamp
) {
}
