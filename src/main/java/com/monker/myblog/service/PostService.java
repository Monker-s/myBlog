package com.monker.myblog.service;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.dto.PostListDto;
import com.monker.myblog.dto.UpsertPostDto;
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
}
