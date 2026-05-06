package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monker.myblog.common.NotificationType;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.dto.CreateCommentDto;
import com.monker.myblog.dto.ReplyCommentDto;
import com.monker.myblog.dto.UserDto;
import com.monker.myblog.entity.Comment;
import com.monker.myblog.entity.Post;
import com.monker.myblog.entity.User;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.CommentMapper;
import com.monker.myblog.mapper.PostMapper;
import com.monker.myblog.mapper.UserMapper;
import com.monker.myblog.service.CommentService;
import com.monker.myblog.service.NotificationPublisher;
import com.monker.myblog.service.PostService;
import com.monker.myblog.util.RedisContents;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.CommentResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import static com.monker.myblog.util.RedisContents.POST_COMMENT;

/**
 * 文件用途：评论业务实现类。
 * 作用说明：提供评论列表滚动加载、新建评论和回复评论功能。
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private NotificationPublisher notificationPublisher;

    /**
     * 函数用途：查询文章评论列表（滚动加载）。
     * 实现逻辑：基于 lastCommentTime 游标查询，使用 Redis ZSET 缓存，同时查询子回复
     *
     * @param postId 文章主键
     * @param lastCommentTime 最后一条评论的时间戳（首次加载传null）
     * @param size 每页数量
     * @return 评论列表
     */
    @Override
    public List<CommentResponse> listComments(Long postId, LocalDateTime lastCommentTime, int size) {
        String cacheKey = POST_COMMENT + postId;
        
        // 尝试从 Redis ZSET 中获取全量缓存
        List<CommentResponse> allCachedComments = getAllCachedComments(cacheKey);
        
        if (allCachedComments != null && !allCachedComments.isEmpty()) {
            // 从全量缓存中按游标截取
            return sliceCommentsByCursor(allCachedComments, lastCommentTime, size);
        }
        
        // 缓存未命中，从数据库查询全量评论
        // 1. 查询一级评论（depth=1）- 查询全部，不分页
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getDepth, 1)    //第一级评论
                .eq(Comment::getStatus, 1); // 只查询已审核的评论
        
        // 按创建时间降序排列，查询全部
        queryWrapper.orderByDesc(Comment::getCreatedAt);
        
        List<Comment> parentComments = commentMapper.selectList(queryWrapper);
        
        if (parentComments == null || parentComments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 批量查询这些一级评论的子回复
        List<Long> parentIds = parentComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Comment> replyQuery = new LambdaQueryWrapper<>();
        replyQuery.in(Comment::getParentId, parentIds)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt); // 子回复按时间升序
        
        List<Comment> replies = commentMapper.selectList(replyQuery);
        
        // 3. 收集所有用户ID（包括一级评论和子回复）
        List<Long> userIds = new ArrayList<>();
        userIds.addAll(parentComments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList()));
        userIds.addAll(replies.stream().map(Comment::getUserId).distinct().collect(Collectors.toList()));
        
        // 4. 批量查询用户信息
        Map<Long, String> userNameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()));
            userNameMap = users.stream()
                    .filter(u -> u.getUsername() != null)  // 过滤掉 username 为 null 的用户
                    .collect(Collectors.toMap(User::getId, User::getUsername, (v1, v2) -> v1));
        }
        
        // 5. 将子回复按 parentId 分组
        Map<Long, List<Comment>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        // 6. 组装树形结构
        final Map<Long, String> finalUserNameMap = userNameMap;
        List<CommentResponse> responseList = parentComments.stream()
                .map(parent -> {
                    List<Comment> childReplies = repliesMap.getOrDefault(parent.getId(), new ArrayList<>());
                    List<CommentResponse> replyResponses = childReplies.stream()
                            .map(reply -> new CommentResponse(
                                    reply.getId(),
                                    reply.getPostId(),
                                    finalUserNameMap.getOrDefault(reply.getUserId(), "用户" + reply.getUserId()),
                                    reply.getUserId(),
                                    reply.getParentId(),
                                    reply.getDepth(),
                                    reply.getContent(),
                                    reply.getCreatedAt(),
                                    new ArrayList<>() // 二级评论不再有子回复
                            ))
                            .collect(Collectors.toList());
                    
                    return new CommentResponse(
                            parent.getId(),
                            parent.getPostId(),
                            finalUserNameMap.getOrDefault(parent.getUserId(), "用户" + parent.getUserId()),
                            parent.getUserId(),
                            parent.getParentId(),
                            parent.getDepth(),
                            parent.getContent(),
                            parent.getCreatedAt(),
                            replyResponses
                    );
                })
                .collect(Collectors.toList());
        
        // 7. 存入 Redis ZSET 缓存（全量缓存，过期时间 30 分钟）
        cacheComments(cacheKey, responseList);
        
        log.info("查询到 {} 条一级评论，已全量缓存", responseList.size());
        // 8. 按游标截取返回
        return sliceCommentsByCursor(responseList, lastCommentTime, size);
    }
    
    /**
     * 从 Redis ZSET 获取全量缓存的评论
     */
    private List<CommentResponse> getAllCachedComments(String cacheKey) {

        try {
            // 获取 ZSET 中所有成员
            Set<String> commentJsons = stringRedisTemplate.opsForZSet()
                    .reverseRange(cacheKey, 0, -1);
            
            if (commentJsons == null || commentJsons.isEmpty()) {
                return null;
            }
            
            // 反序列化为 CommentResponse 列表
            List<CommentResponse> comments = new ArrayList<>();
            for (String json : commentJsons) {
                CommentResponse comment = objectMapper.readValue(json, CommentResponse.class);
                comments.add(comment);
            }
            
            return comments;
        } catch (Exception e) {
            log.error("从缓存获取全部评论失败", e);
            return null;
        }
    }
    
    /**
     * 从全量评论列表中按游标截取指定数量的评论
     */
    private List<CommentResponse> sliceCommentsByCursor(List<CommentResponse> allComments, 
                                                         LocalDateTime lastCommentTime, 
                                                         int size) {
        List<CommentResponse> result;
        
        if (lastCommentTime == null) {
            // 首次加载：返回最新的 size 条
            result = allComments.stream()
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            // 加载更多：找到 lastCommentTime 之后的评论
            result = allComments.stream()
                    .filter(comment -> comment.createdAt().isBefore(lastCommentTime))
                    .limit(size)
                    .collect(Collectors.toList());
        }
        
        log.info("从缓存中截取 {} 条评论（总共 {} 条）", result.size(), allComments.size());
        return result;
    }
    
    /**
     * 从 Redis ZSET 获取缓存的评论（旧方法，保留兼容）
     */
    private List<CommentResponse> getCachedComments(String cacheKey, LocalDateTime lastCommentTime, int size) {
        log.info("从缓存中获取评论，缓存 key：{}", cacheKey);
        try {
            double minScore;
            double maxScore;
            
            if (lastCommentTime == null) {
                // 首次加载：获取最新的 size 条
                minScore = 0;
                maxScore = Double.MAX_VALUE;
            } else {
                // 加载更多：获取比 lastCommentTime 更早的评论
                minScore = 0;
                maxScore = lastCommentTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
            }
            
            // 从 ZSET 中按 score 降序获取数据（revrangeByScore）
            Set<String> commentJsons = stringRedisTemplate.opsForZSet()
                    .reverseRangeByScore(cacheKey, minScore, maxScore, 0, size);
            
            if (commentJsons == null || commentJsons.isEmpty()) {
                return null;
            }
            
            // 反序列化为 CommentResponse 列表
            List<CommentResponse> comments = new ArrayList<>();
            for (String json : commentJsons) {
                CommentResponse comment = objectMapper.readValue(json, CommentResponse.class);
                comments.add(comment);
            }

            return comments;
        } catch (Exception e) {
            log.error("从缓存获取评论失败", e);
            return null;
        }
    }
    
    /**
     * 将评论存入 Redis ZSET 缓存
     */
    private void cacheComments(String cacheKey, List<CommentResponse> comments) {
        try {
            log.info("缓存 {} 条评论到 Redis ZSET", comments.size());
            // 先删除旧的缓存
            stringRedisTemplate.delete(cacheKey);
            
            // 将每条评论存入 ZSET，score 为 createdAt 的时间戳
            for (CommentResponse comment : comments) {
                String json = objectMapper.writeValueAsString(comment);
                double score = comment.createdAt().atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
                stringRedisTemplate.opsForZSet().add(cacheKey, json, score);
            }
            
            // 设置过期时间为 30 分钟
            stringRedisTemplate.expire(cacheKey, 30, java.util.concurrent.TimeUnit.MINUTES);
            
            log.info("缓存 {} 条评论到 Redis ZSET", comments.size());
        } catch (JsonProcessingException e) {
            log.error("缓存评论失败", e);
        }
    }

    /**
     * 函数用途：创建一级评论。
     *
     * @param postId 文章主键
     * @param request 评论参数
     * @return 新建后的评论
     */
    @Override
    public CommentResponse createComment(Long postId, CreateCommentDto request) {
        // 获取当前登录用户信息
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再评论");
        }
        
        Long userId = currentUser.getId();
        String username = currentUser.getUsername();
        LocalDateTime now = LocalDateTime.now();
        
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentId(null)
                .depth(1)
                .content(request.content())
                .status(1) // 默认审核通过
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        commentMapper.insert(comment);
        
        // 更新数据库posts的评论数
        updatePostCommentCount(postId);

        // 清除缓存
        String cacheKey = POST_COMMENT + postId;
        stringRedisTemplate.delete(cacheKey);
        
        // 发送通知给文章作者（如果不是自己评论自己的文章）
        Post post = postMapper.selectById(postId);
        if (post != null && !post.getAuthorId().equals(userId)) {
            notificationPublisher.publishPersonalNotification(
                    post.getAuthorId(),
                    NotificationType.COMMENT_ON_POST,
                    postId,
                    "新评论",
                    "用户 " + username + " 评论了你的文章",
                    null
            );
        }
        
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                username,
                comment.getUserId(),
                comment.getParentId(),
                comment.getDepth(),
                comment.getContent(),
                comment.getCreatedAt(),
                new ArrayList<>()
        );
    }

    /**
     * 函数用途：创建二级回复评论。
     *
     * @param commentId 父评论主键
     * @param request 回复参数
     * @return 新建后的回复评论
     */
    @Override
    public CommentResponse replyComment(Long commentId, ReplyCommentDto request) {
        // 查询父评论
        Comment parentComment = commentMapper.selectById(commentId);
        if (parentComment == null) {
            throw new RuntimeException("父评论不存在");
        }
        
        // 验证父评论必须是一级评论（depth=1）
        if (parentComment.getDepth() == null || parentComment.getDepth() != 1) {
            throw new RuntimeException("只能回复一级评论");
        }
        
        // 获取当前登录用户信息
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再回复");
        }
        
        Long userId = currentUser.getId();
        String username = currentUser.getUsername();
        LocalDateTime now = LocalDateTime.now();
        
        Comment reply = Comment.builder()
                .postId(parentComment.getPostId())
                .userId(userId)
                .parentId(commentId)
                .depth(null)  // 由数据库触发器自动设置为 2
                .content(request.content())
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        commentMapper.insert(reply);
        
        // 重新查询获取触发器设置的 depth 值
        Comment insertedReply = commentMapper.selectById(reply.getId());
        if (insertedReply == null) {
            throw new RuntimeException("回复创建失败");
        }
        
        // 更新数据库posts的评论数
        updatePostCommentCount(parentComment.getPostId());
        
        // 清除缓存
        String cacheKey = POST_COMMENT + parentComment.getPostId();
        stringRedisTemplate.delete(cacheKey);
        
        // 发送通知给被回复的评论作者（如果不是自己回复自己）
        if (!parentComment.getUserId().equals(userId)) {
            log.info("准备发送评论回复通知 - 父评论ID: {}, 父评论作者ID: {}, 回复者ID: {}", 
                    commentId, parentComment.getUserId(), userId);
            
            notificationPublisher.publishPersonalNotification(
                    parentComment.getUserId(),
                    NotificationType.COMMENT_REPLY,
                    parentComment.getPostId(),
                    "新回复",
                    "用户 " + username + " 回复了你的评论",
                    null
            );
            
            log.info("评论回复通知已发送 - 父评论ID: {}", commentId);
        } else {
            log.debug("自己回复自己的评论，不发送通知 - 评论ID: {}", commentId);
        }
        
        return new CommentResponse(
                insertedReply.getId(),
                insertedReply.getPostId(),
                username,
                insertedReply.getUserId(),
                insertedReply.getParentId(),
                insertedReply.getDepth(),
                insertedReply.getContent(),
                insertedReply.getCreatedAt(),
                new ArrayList<>()
        );
    }

    /**
     * 函数用途：更新文章的评论数。
     *
     * @param postId 文章主键
     */
    private void updatePostCommentCount(Long postId) {
        // 查询当前文章的评论总数（只统计已审核的评论）
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1); // 只统计已审核的评论
        
        long commentCount = commentMapper.selectCount(queryWrapper);
        
        // 更新posts表的comment_count字段
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount((int) commentCount);
            postMapper.updateById(post);
            log.info("更新文章评论数成功，postId={}, commentCount={}", postId, commentCount);
        }
    }
}
