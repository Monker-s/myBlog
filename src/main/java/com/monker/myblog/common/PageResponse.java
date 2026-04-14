package com.monker.myblog.common;

import java.util.List;

/**
 * 文件用途：定义分页查询响应结构。
 * 作用说明：统一返回记录列表、总数、页码和分页大小，便于前端直接渲染列表页。
 *
 * @param <T> 分页记录类型
 */
public record PageResponse<T>(
        List<T> records,
        long total,
        int page,
        int size
) {

    /**
     * 函数用途：快速创建一个空分页响应。
     *
     * @param page 当前页码
     * @param size 分页大小
     * @return 空数据的分页结果
     * @param <T> 分页记录类型
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), 0, page, size);
    }
}
