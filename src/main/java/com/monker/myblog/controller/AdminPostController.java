package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.dto.UpsertPostDto;
import com.monker.myblog.service.PostService;
import com.monker.myblog.vo.PostDetailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供后台文章新增和更新接口。
 * 作用说明：供 CMS 管理端调用，负责把文章写入请求交给 {@link PostService} 处理。
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService postService;

    /**
     * 函数用途：注入文章服务。
     *
     * @param postService 文章服务
     */
    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 函数用途：新增文章。
     *
     * @param request 新增文章参数
     * @return 新增后的文章详情
     */
    @PostMapping
    public Result<PostDetailResponse> createPost(@Valid @RequestBody UpsertPostDto request) {
        return Result.success(postService.savePost(null, request));
    }

    /**
     * 函数用途：更新指定文章。
     *
     * @param postId 文章主键
     * @param request 更新文章参数
     * @return 更新后的文章详情
     */
    @PutMapping("/{postId}")
    public Result<PostDetailResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpsertPostDto request
    ) {
        return Result.success(postService.savePost(postId, request));
    }
}
