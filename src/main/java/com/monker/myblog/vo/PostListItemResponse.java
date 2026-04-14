package com.monker.myblog.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件用途：文章列表项响应对象。
 * 关联接口：PostController.list、AdminPostController.list。
 * 作用说明：返回文章列表页所需的简要展示字段。
 *
 * @param id 文章主键
 * @param title 文章标题
 * @param summary 文章摘要
 * @param coverUrl 封面地址
 * @param categoryId 分类主键
 * @param tags 标签名称列表
 * @param likeCount 点赞数
 * @param viewCount 浏览数
 * @param pinned 是否置顶
 * @param updatedAt 更新时间
 */
public record PostListItemResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        Long categoryId,
        List<String> tags,
        Integer likeCount,
        Integer viewCount,
        Boolean pinned,
        LocalDateTime updatedAt
) {
}
