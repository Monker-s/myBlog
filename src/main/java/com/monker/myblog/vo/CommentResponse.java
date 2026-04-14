package com.monker.myblog.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件用途：评论树节点响应对象。
 * 关联接口：PostCommentController.list、CommentReplyController.reply。
 * 作用说明：按树形结构返回评论主体与其子回复列表。
 *
 * @param id 评论主键
 * @param postId 所属文章主键
 * @param userId 评论用户主键
 * @param parentId 父评论主键，一级评论时为空
 * @param depth 当前评论层级
 * @param content 评论正文
 * @param createdAt 评论创建时间
 * @param replies 子回复列表
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record CommentResponse(
        Long id,
        Long postId,
        String userName,  // 新增字段
        Long userId,
        Long parentId,
        Integer depth,
        String content,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
}
