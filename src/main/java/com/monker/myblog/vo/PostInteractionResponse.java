package com.monker.myblog.vo;

/**
 * 文件用途：文章互动统计响应对象。
 * 关联接口：PostController.toggleLike、PostController.detail。
 * 作用说明：返回文章点赞和浏览等轻量互动数据。
 *
 * @param postId 文章主键
 * @param likeCount 当前点赞数
 * @param viewCount 当前浏览数
 * @param liked 当前用户是否已点赞
 */
public record PostInteractionResponse(
        Long postId,
        Integer likeCount,
        Integer viewCount,
        Boolean liked
) {
}
