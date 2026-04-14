package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.dto.ReplyCommentDto;
import com.monker.myblog.service.CommentService;
import com.monker.myblog.vo.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供评论回复接口。
 * 作用说明：负责接收二级评论回复请求，并交由 {@link CommentService} 处理回复逻辑。
 */
@RestController
@RequestMapping("/api/comments")
public class CommentReplyController {

    private final CommentService commentService;

    /**
     * 函数用途：注入评论服务。
     *
     * @param commentService 评论服务
     */
    public CommentReplyController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 函数用途：回复指定评论。
     *
     * @param commentId 被回复的评论主键
     * @param request 回复参数
     * @return 回复后的评论结果
     */
    @PostMapping("/{commentId}/reply")
    public Result<CommentResponse> replyComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyCommentDto request
    ) {
        return Result.success(commentService.replyComment(commentId, request));
    }
}
