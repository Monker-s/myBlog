package com.monker.myblog.controller;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.common.Result;
import com.monker.myblog.dto.AdminPostListDto;
import com.monker.myblog.dto.UpsertPostDto;
import com.monker.myblog.service.PostService;
import com.monker.myblog.vo.AdminPostDetailResponse;
import com.monker.myblog.vo.AdminPostListItemResponse;
import com.monker.myblog.vo.PostDetailResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.monker.myblog.util.RedisContents.POST_DETAIL;
import static com.monker.myblog.util.RedisContents.POST_LIST;
import static com.monker.myblog.util.RedisContents.POST_LIST_PINNED;

/**
 * 文件用途：提供后台文章新增和更新接口。
 * 作用说明：供 CMS 管理端调用，负责把文章写入请求交给 {@link PostService} 处理。
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    @Autowired
    private PostService postService;

    /**
     * 函数用途：管理后台查询文章分页列表。
     *
     * @param query 列表筛选与分页参数
     * @return 文章分页列表
     */
    @GetMapping
    public Result<PageResponse<AdminPostListItemResponse>> listPosts(@Valid AdminPostListDto query) {
        return Result.success(postService.listAdminPosts(query));
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

    /**
     * 函数用途：管理后台查询文章详情。
     *
     * @param postId 文章主键
     * @return 文章详情（包含分类和标签的完整信息）
     */
    @GetMapping("/{postId}")
    public Result<AdminPostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return Result.success(postService.getAdminPostDetail(postId));
    }

    /**
     * 函数用途：手动触发文章浏览量同步（测试用）。
     *
     * @return 同步结果
     */
    @PostMapping("/sync-view-count")
    public Result<String> syncViewCount() {
        postService.syncViewCountToDatabase();
        return Result.success("浏览量同步成功");
    }

    /**
     * 函数用途：管理后台删除文章。
     *
     * @param postId 文章主键
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success(null);
    }
}
