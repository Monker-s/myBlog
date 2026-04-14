package com.monker.myblog.service;

import com.monker.myblog.dto.CreateCommentDto;
import com.monker.myblog.dto.ReplyCommentDto;
import com.monker.myblog.vo.CommentResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件用途：定义评论业务能力。
 * 作用说明：统一对外暴露评论列表查询、新建评论和回复评论等操作。
 */
public interface CommentService {

    /**
     * 函数用途：查询指定文章的评论列表（滚动加载）。
     *
     * @param postId 文章主键
     * @param lastCommentTime 最后一条评论的时间戳（首次加载传null）
     * @param size 每页数量
     * @return 评论列表
     */
    List<CommentResponse> listComments(Long postId, LocalDateTime lastCommentTime, int size);

    /**
     * 函数用途：创建一级评论。
     *
     * @param postId 文章主键
     * @param request 评论参数
     * @return 新建后的评论
     */
    CommentResponse createComment(Long postId, CreateCommentDto request);

    /**
     * 函数用途：创建二级回复评论。
     *
     * @param commentId 父评论主键
     * @param request 回复参数
     * @return 新建后的回复评论
     */
    CommentResponse replyComment(Long commentId, ReplyCommentDto request);
}
