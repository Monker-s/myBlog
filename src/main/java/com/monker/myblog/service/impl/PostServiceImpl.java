package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.monker.myblog.common.PageResponse;
import com.monker.myblog.common.PostContents;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.dto.PostListDto;
import com.monker.myblog.dto.UpsertPostDto;
import com.monker.myblog.entity.Post;
import com.monker.myblog.entity.PostViewDedupDaily;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.PostLikeMapper;
import com.monker.myblog.mapper.PostMapper;
import com.monker.myblog.mapper.PostViewDedupDailyMapper;
import com.monker.myblog.mapper.TagMapper;
import com.monker.myblog.service.PostService;
import com.monker.myblog.util.JsonUtil;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.PostDetailResponse;
import com.monker.myblog.vo.PostGetLikeResponse;
import com.monker.myblog.vo.PostInteractionResponse;
import com.monker.myblog.vo.PostListItemResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.monker.myblog.util.RedisContents.*;

/**
 * 文件用途：文章业务实现类。
 * 作用说明：当前提供文章模块的骨架实现，后续会在这里接入文章表、标签关系、点赞和浏览统计逻辑。
 */

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostViewDedupDailyMapper postViewDedupDailyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PostLikeMapper postLikeMapper;

    /**
     * 函数用途：查询文章分页列表。
     *
     * @param query 列表筛选与分页参数
     * @return 文章分页结果
     */
    @Override
    public PageResponse<PostListItemResponse> listPosts(PostListDto query) {
        // 处理默认值
        int page = query.getPage() != null ? query.getPage() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        if (query.getPinned() !=null){
            //说明只要置顶
            String key = POST_LIST_PINNED + page + ":" + size;
            //查看是否有缓存
            String s = stringRedisTemplate.opsForValue().get(key);
            if (s != null) {
                //命中缓存
                return JsonUtil.fromJson(s, PageResponse.class);
            }
            //未命中缓存
            //查数据库
            List<Post> posts = postMapper.selectByCondition(
                    query.getKeyword(),
                    query.getCategoryId(),
                    PostContents.POST_STATUS, // status = 1 表示已发布
                    query.getPinned() // 是否只查询置顶文章
            );
            PageResponse<PostListItemResponse> response = new PageResponse<>(
                    posts.stream()
                            .map(this::convertToListItem)
                            .collect(Collectors.toList()),
                    posts.size(),
                    page,
                    size
            );

            // 存入缓存，设置过期时间为 30 分钟
            stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(response), 30, TimeUnit.MINUTES);
            return response;
        }
        //查询缓存
        String key = POST_LIST + page + ":" + size;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            //命中缓存
            return JsonUtil.fromJson(value, PageResponse.class);
        }
        //未命中缓存
        // 开启分页
        PageHelper.startPage(page, size);
        // 查询数据（只查询已发布的文章）
        List<Post> posts = postMapper.selectByCondition(
                query.getKeyword(),
                query.getCategoryId(),
                PostContents.POST_STATUS, // status = 1 表示已发布
                query.getPinned() // 是否只查询置顶文章
        );
        // 获取分页信息
        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        // 转换为 VO
        List<PostListItemResponse> records = posts.stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());
        PageResponse<PostListItemResponse> response = new PageResponse<>(
                records,
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
        
        // 存入缓存，设置过期时间为 30 分钟
        stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(response), 30, TimeUnit.MINUTES);
        
        return response;
    }

    /**
     * 函数用途：将 Post 实体转换为列表项 VO。
     *
     * @param post 文章实体
     * @return 列表项 VO
     */
    private PostListItemResponse convertToListItem(Post post) {
        // 查询标签列表
        List<String> tags = tagMapper.selectNamesByPostId(post.getId());
        
        return new PostListItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCoverUrl(),
                post.getCategoryId(),
                tags,
                post.getLikeCount() != null ? post.getLikeCount() : 0,
                post.getViewCount() != null ? post.getViewCount() : 0,
                post.getPinned() != null && post.getPinned(),
                post.getUpdatedAt()
        );
    }

    /**
     * 函数用途：查询文章详情。
     *
     * @param postId 文章主键
     * @return 文章详情
     */
    @Override
    public PostDetailResponse getPostDetail(Long postId) {
        //先查缓存
        String key = POST_DETAIL + postId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            //命中缓存
            return JsonUtil.fromJson(value, PostDetailResponse.class);
        }
        
        //未命中缓存
        // 根据id查文章
        Post post = postMapper.selectByIdAndDeletedAtIsNull(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在或已删除");
        }
        
        // 查询标签列表
        List<String> tags = tagMapper.selectNamesByPostId(postId);
        
        // 检查当前用户是否已点赞（未登录用户默认为false）
        Boolean liked = false;
        try {
            Long userId = UserHolder.getUserId().getId();
            if (userId != null) {
                String likeKey = POST_LIKE_USER + postId;
                liked = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(likeKey, userId.toString()));
            }
        } catch (Exception e) {
            // 未登录或获取用户信息失败，默认为未点赞
            liked = false;
        }
        
        // 封装文章信息
        PostDetailResponse response = new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCoverUrl(),
                post.getContentHtml(),
                post.getTocJson(),
                post.getAuthorId(),
                post.getCategoryId(),
                tags,
                post.getLikeCount() != null ? post.getLikeCount() : 0,
                post.getViewCount() != null ? post.getViewCount() : 0,
                post.getCommentCount() != null ? post.getCommentCount() : 0,
                liked,
                post.getPinned() != null && post.getPinned(),
                post.getPublishedAt(),
                post.getUpdatedAt()
        );
        
        // 存入缓存，设置过期时间为 30 分钟
        stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(response), 30, TimeUnit.MINUTES);
        return response;
    }

    /**
     * 函数用途：执行文章点赞/取消点赞（只操作 Redis）。
     *
     * @param postId 文章主键
     * @return 点赞后的交互结果
     */
    @Override
    public PostInteractionResponse likePost(Long postId) {
        // 获取当前用户ID
        Long userId = UserHolder.getUserId().getId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
            
        // 查询文章（用于验证文章存在性）
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
            
        String key = POST_LIKE_USER + postId;
        // 查询用户是否点赞过该文章
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        log.info("用户 {} 是否点赞过文章 {}：{}", userId, postId, isLiked);
        boolean liked;
        if (Boolean.TRUE.equals(isLiked)) {
            // 已经点赞，取消点赞
            stringRedisTemplate.opsForSet().remove(key, userId.toString());
            // 数据库点赞数减1（原子更新）
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.setSql("like_count = like_count - 1");
            postMapper.update(null, updateWrapper);
            liked = true;
        } else {
            // 未点赞，添加点赞
            stringRedisTemplate.opsForSet().add(key, userId.toString());
            // 数据库点赞数加1（原子更新）
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.setSql("like_count = like_count + 1");
            postMapper.update(null, updateWrapper);
            log.info("用户 {} 点赞文章 {},已更新数据库", userId, postId);
            liked = false;
        }
        int newLikeCount = post.getLikeCount();
        return new PostInteractionResponse(postId, newLikeCount, 0, liked);
    }




    /**
     * 函数用途：记录文章浏览（支持30分钟去重）。
     * 实现逻辑：
     * 1. 生成用户指纹哈希（基于用户ID或IP+UA）
     * 2. 查询该用户是否在30分钟内浏览过此文章
     * 3. 如果未浏览或超过30分钟，则增加浏览量并记录
     *
     * @param postId 文章主键
     * @return 浏览后的交互结果
     */
    @Override
    public PostInteractionResponse recordView(Long postId) {
        // 验证文章是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        
        // 生成用户指纹哈希（登录用户用userId，未登录用户用IP+UA）
        String dedupKeyHash;
        try {
            Long userId = UserHolder.getUserId().getId();
            String rawKey;
            if (userId != null) {
                // 已登录用户，使用用户ID作为指纹
                rawKey = "user:" + userId;
            } else {
                // 未登录用户，暂时使用匿名标识（实际项目中应从请求头获取IP和UA）
                rawKey = "anonymous:default";
            }
            dedupKeyHash = sha256(rawKey);
        } catch (Exception e) {
            // 异常情况使用默认指纹
            dedupKeyHash = sha256("anonymous:error");
        }
        
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
        
        // 查询今天是否已有浏览记录
        PostViewDedupDaily existingRecord = postViewDedupDailyMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostViewDedupDaily>()
                .eq(PostViewDedupDaily::getPostId, postId)
                .eq(PostViewDedupDaily::getDedupDate, today)
                .eq(PostViewDedupDaily::getDedupKeyHash, dedupKeyHash)
        );
        
        boolean shouldIncrement = false;
        
        if (existingRecord == null) {
            // 没有今天的浏览记录，需要增加浏览量
            shouldIncrement = true;
            
            // 创建新的浏览记录
            PostViewDedupDaily newRecord = PostViewDedupDaily.builder()
                .postId(postId)
                .dedupDate(today)
                .dedupKeyHash(dedupKeyHash)
                .createdAt(now)
                .lastViewedAt(now)
                .build();
            postViewDedupDailyMapper.insert(newRecord);
        } else {
            // 有今天的浏览记录，检查是否超过30分钟
            LocalDateTime lastViewedAt = existingRecord.getLastViewedAt();
            if (lastViewedAt == null || lastViewedAt.isBefore(thirtyMinutesAgo)) {
                // 超过30分钟，需要增加浏览量并更新最后浏览时间
                shouldIncrement = true;
                
                existingRecord.setLastViewedAt(now);
                postViewDedupDailyMapper.updateById(existingRecord);
            }
            // 否则在30分钟内，不增加浏览量
        }
        
        // 如果需要增加浏览量，则在Redis中自增
        Integer viewCount = post.getViewCount() != null ? post.getViewCount() : 0;
        if (shouldIncrement) {
            String key = POST_VIEW_COUNT + postId;
            stringRedisTemplate.opsForValue().increment(key);
            viewCount = viewCount + 1;
        }
        
        return new PostInteractionResponse(postId, 0, viewCount, false);
    }
    
    /**
     * 函数用途：计算字符串的SHA256哈希值。
     *
     * @param input 输入字符串
     * @return SHA256哈希值的十六进制表示
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    /**
     * 函数用途：模拟新增或更新文章。
     *
     * @param postId 文章主键，新增时可为空
     * @param request 文章写入参数
     * @return 保存后的文章详情
     */
    @Override
    public PostDetailResponse savePost(Long postId, UpsertPostDto request) {
        return new PostDetailResponse(
                postId == null ? 1L : postId,
                request.title(),
                request.summary(),
                request.coverUrl(),
                request.contentHtml(),
                request.tocJson(),
                1L,
                request.categoryId(),
                List.of(),
                0,
                0,
                0,
                false,
                Boolean.TRUE.equals(request.pinned()),
                request.status() == 1 ? LocalDateTime.now() : null,
                LocalDateTime.now()
        );
    }

    /**
     * 函数用途：获取文章点赞状态。
     *
     * @param postId 文章主键
     * @return 点赞状态
     */
    @Override
    public PostGetLikeResponse getPostLikeStatus(Long postId) {

        // 验证文章是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        //没有就从数据库拿
        // 从数据库获取点赞数
        Integer likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
            
        // 从 Redis 检查当前用户是否已点赞
        boolean liked = false;
        try {
            Long userId = UserHolder.getUserId().getId();
            if (userId != null) {
                String likeKey = POST_LIKE_USER + postId;

                liked = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(likeKey, userId.toString()));
            }
            log.info("获取文章点赞状态：userId{}",userId);
        } catch (Exception e) {
            // 未登录或获取用户信息失败，默认为未点赞
            liked = false;
        }
        log.info("获取文章点赞状态：{},{}",liked,likeCount);
        return new PostGetLikeResponse(likeCount,liked);
    }
}
