package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.dto.CreateCommentDto;
import com.monker.myblog.service.CommentService;
import com.monker.myblog.vo.CommentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.monker.myblog.common.PostContents.POST_COMMENT_PAGESIZE;

/**
 * 文件用途：提供文章评论列表和发表评论接口。
 * 作用说明：围绕指定文章处理评论相关的主入口请求，并调用 {@link CommentService} 完成业务处理。
 */
@Validated
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Slf4j
public class PostCommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 函数用途：查询指定文章的评论列表（滚动加载）。
     *
     * @param postId 文章主键
     * @param lastCommentTime 最后一条评论的时间戳（首次加载传null）
     * @return 评论列表
     */
    @GetMapping
    public Result<List<CommentResponse>> listComments(
            @PathVariable Long postId,
            @RequestParam(required = false) LocalDateTime lastCommentTime
    ) {
        log.info("获取文章评论列表 - postId:{}, lastCommentTime:{}", postId, lastCommentTime);
        return Result.success(commentService.listComments(postId, lastCommentTime, POST_COMMENT_PAGESIZE));
    }

    /**
     * 函数用途：为指定文章创建一级评论。
     *
     * @param postId 文章主键
     * @param request 评论参数
     * @return 新建后的评论结果
     */
    @PostMapping
    public Result<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentDto request
    ) {
        log.info("创建文章评论:{},{}", postId,request);
        return Result.success(commentService.createComment(postId, request));
    }
}
