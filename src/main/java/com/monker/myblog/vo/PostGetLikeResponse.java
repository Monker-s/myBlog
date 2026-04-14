package com.monker.myblog.vo;

/**
 * 文件用途：文章点赞状态响应对象。
 * 关联接口：PostController.getPostLikeStatus。
 * 作用说明：返回文章点赞数和当前用户点赞状态。
 *
 * @param likeCount 当前点赞数
 * @param liked 当前用户是否已点赞
 */
public record PostGetLikeResponse(
        Integer likeCount,
        Boolean liked
) {
}
