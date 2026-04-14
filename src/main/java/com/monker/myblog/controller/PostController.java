package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.PostListDto;
import com.monker.myblog.service.PostService;
import com.monker.myblog.vo.PostDetailResponse;
import com.monker.myblog.vo.PostGetLikeResponse;
import com.monker.myblog.vo.PostInteractionResponse;
import com.monker.myblog.vo.PostListItemResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供文章列表、详情、点赞和浏览上报接口。
 * 作用说明：对外暴露文章相关的公开能力，并将请求转发到 {@link PostService}。
 */
@Validated
@RestController
@RequestMapping("/api/posts")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 函数用途：处理文章列表查询请求。
     *
     * @param query 列表筛选与分页参数
     * @return 文章分页列表
     */
    @GetMapping
    public Result<PageResponse<PostListItemResponse>> listPosts(@Valid PostListDto query) {
        log.info("查询文章列表,{}", query.getPinned());
        return Result.success(postService.listPosts(query));
    }

    /**
     * 函数用途：处理文章详情查询请求。
     *
     * @param postId 文章主键
     * @return 文章详情视图
     */
    @GetMapping("/{postId}")
    public Result<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        log.info("查询文章详细：{}", postId);
        return Result.success(postService.getPostDetail(postId));
    }

    /**
     * 函数用途：处理文章点赞请求。
     *
     * @param postId 文章主键
     * @return 点赞后的交互结果
     */
    @PostMapping("/{postId}/like")
    public Result<PostInteractionResponse> likePost(@PathVariable Long postId) {
        log.info("点赞文章：{}", postId);
        return Result.success(postService.likePost(postId));
    }

    /**
     * 函数用途：获取文章点赞状态。
     *
     * @path postId 文章主键
     *
     * @return 保存后的文章点赞详细
     */
    @GetMapping("/{postId}/like")
    public Result<PostGetLikeResponse> getPostLikeStatus(@PathVariable Long postId) {
        log.info("获取文章点赞状态：{}", postId);
        return Result.success(postService.getPostLikeStatus(postId));
    }
    /**
     * 函数用途：处理文章浏览上报请求。
     *
     * @param postId 文章主键
     * @return 浏览后的交互结果
     */
    @PostMapping("/{postId}/view")
    public Result<PostInteractionResponse> recordView(@PathVariable Long postId) {
        log.info("文章浏览量：{}", postId);
        return Result.success(postService.recordView(postId));
    }
}
