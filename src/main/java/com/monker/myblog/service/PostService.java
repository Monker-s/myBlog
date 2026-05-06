package com.monker.myblog.service;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.AdminPostListDto;
import com.monker.myblog.dto.PostListDto;
import com.monker.myblog.dto.UpsertPostDto;
import com.monker.myblog.vo.AdminPostDetailResponse;
import com.monker.myblog.vo.AdminPostListItemResponse;
import com.monker.myblog.vo.PostDetailResponse;
import com.monker.myblog.vo.PostGetLikeResponse;
import com.monker.myblog.vo.PostInteractionResponse;
import com.monker.myblog.vo.PostListItemResponse;

/**
 * 文件用途：定义文章业务能力。
 * 作用说明：统一提供文章列表、详情、点赞、浏览统计和后台保存等核心行为。
 */
public interface PostService {

    /**
     * 函数用途：查询文章分页列表。
     *
     * @param query 列表筛选与分页参数
     * @return 文章分页结果
     */
    PageResponse<PostListItemResponse> listPosts(PostListDto query);

    /**
     * 函数用途：管理后台查询文章分页列表。
     *
     * @param query 管理后台列表筛选与分页参数
     * @return 文章分页结果
     */
    PageResponse<AdminPostListItemResponse> listAdminPosts(AdminPostListDto query);

    /**
     * 函数用途：查询文章详情。
     *
     * @param postId 文章主键
     * @return 文章详情
     */
    PostDetailResponse getPostDetail(Long postId);

    /**
     * 函数用途：处理文章点赞。
     *
     * @param postId 文章主键
     * @return 点赞后的交互信息
     */
    PostInteractionResponse likePost(Long postId);

    /**
     * 函数用途：记录文章浏览行为。
     *
     * @param postId 文章主键
     * @return 浏览上报结果
     */
    PostInteractionResponse recordView(Long postId);

    /**
     * 函数用途：新增或更新文章。
     *
     * @param postId 文章主键，新增时可为空
     * @param request 文章写入参数
     * @return 保存后的文章详情
     */
    PostDetailResponse savePost(Long postId, UpsertPostDto request);

    /**
     * 函数用途：查询文章点赞状态。
     *
     * @param postId 文章主键
     * @return 点赞状态
     */
    PostGetLikeResponse getPostLikeStatus(Long postId);

    /**
     * 函数用途：获取文章统一统计信息（点赞数、浏览量、点赞状态）。
     * 作用说明：从 Redis 读取最新数据，并同步到数据库，保证数据一致性。
     *
     * @param postId 文章主键
     * @return 文章统计信息
     */
    PostInteractionResponse getPostStats(Long postId);

    /**
     * 函数用途：管理后台查询文章详情。
     *
     * @param postId 文章主键
     * @return 文章详情（包含分类和标签的完整信息）
     */
    AdminPostDetailResponse getAdminPostDetail(Long postId);

    /**
     * 函数用途：将 Redis 中的浏览量数据同步到数据库。
     * 作用说明：定时任务调用，批量更新所有文章的浏览量。
     */
    void syncViewCountToDatabase();

    /**
     * 函数用途：管理后台删除文章。
     *
     * @param postId 文章主键
     */
    void deletePost(Long postId);
}
