package com.monker.myblog.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件用途：文章详情响应对象。
 * 关联接口：PostController.detail、AdminPostController.detail。
 * 作用说明：聚合文章正文、分类标签和互动统计，供详情页直接渲染。
 *
 * @param id 文章主键
 * @param title 文章标题
 * @param summary 文章摘要
 * @param coverUrl 封面地址
 * @param contentHtml 正文 HTML
 * @param tocJson 目录 JSON
 * @param authorId 作者主键
 * @param categoryId 分类主键
 * @param tags 标签名称列表
 * @param likeCount 点赞数
 * @param viewCount 浏览数
 * @param commentCount 评论数
 * @param liked 当前用户是否已点赞
 * @param pinned 是否置顶
 * @param publishedAt 发布时间
 * @param updatedAt 更新时间
 */
public record PostDetailResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String contentHtml,
        String tocJson,
        Long authorId,
        Long categoryId,
        List<String> tags,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        Boolean liked,    // 当前用户是否已点赞
        Boolean pinned,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
