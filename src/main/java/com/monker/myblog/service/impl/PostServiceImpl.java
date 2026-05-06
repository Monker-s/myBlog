package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.monker.myblog.common.PageResponse;
import com.monker.myblog.common.PostContents;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.dto.AdminPostListDto;
import com.monker.myblog.dto.PostListDto;
import com.monker.myblog.dto.UpsertPostDto;
import com.monker.myblog.dto.UserDto;
import com.monker.myblog.entity.*;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.CategoryMapper;
import com.monker.myblog.mapper.PostLikeMapper;
import com.monker.myblog.mapper.PostMapper;
import com.monker.myblog.mapper.PostTagMapper;
import com.monker.myblog.mapper.PostViewDedupDailyMapper;
import com.monker.myblog.mapper.TagMapper;
import com.monker.myblog.service.NotificationPublisher;
import com.monker.myblog.service.PostService;
import com.monker.myblog.util.JsonUtil;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.AdminPostDetailResponse;
import com.monker.myblog.vo.AdminPostListItemResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.monker.myblog.util.RedisContents.POST_DATA;
import static com.monker.myblog.util.RedisContents.POST_DETAIL;
import static com.monker.myblog.util.RedisContents.POST_LIKE_NOT;
import static com.monker.myblog.util.RedisContents.POST_LIKE_USER;
import static com.monker.myblog.util.RedisContents.POST_LIST;
import static com.monker.myblog.util.RedisContents.POST_LIST_PINNED;

/**
 * 文件用途：文章业务实现类。
 * 作用说明：统一处理文章列表、详情、点赞、浏览统计等核心逻辑。
 * 当前点赞链路遵循“数据库为真值，Redis 为缓存”的原则，优先保证前端读到的数据一致。
 */
@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private static final long POST_CACHE_MINUTES = 30L;
    private static final long NOT_LIKED_CACHE_MINUTES = 5L;  // 未点赞状态缓存时间（较短）

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private PostViewDedupDailyMapper postViewDedupDailyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostTagMapper postTagMapper;
    
    @Autowired
    private NotificationPublisher notificationPublisher;

    /**
     * 函数用途：查询文章分页列表。
     *
     * @param query 列表筛选与分页参数
     * @return 文章分页结果
     */
    @Override
    public PageResponse<PostListItemResponse> listPosts(PostListDto query) {
        int page = query.getPage() != null ? query.getPage() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        
        // 兼容 category 和 categoryId 两种参数名
        Long categoryId = query.getCategoryId();
        if (categoryId == null && query.getCategory() != null) {
            categoryId = query.getCategory();
        }
        
        // 解析排序参数
        String orderBy = parsePublicSortParameter(query.getSort());
        
        //如果有其他条件的都直接查数据库（keyword和category为空字符串时也视为无条件）
        boolean hasKeyword = query.getKeyword() != null && !query.getKeyword().trim().isEmpty();
        boolean hasCategory = categoryId != null;
        
        if (hasKeyword || hasCategory) {
           //条件查询用的时候不多所有没必要用缓存
            PageResponse<PostListItemResponse> extracted = extracted(query, page, size, categoryId, orderBy);
            return extracted;
        }
        if (query.getPinned() != null) {
            String key = POST_LIST_PINNED + page + ":" + size;
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("命中置顶列表缓存，key={}", key);
                return JsonUtil.fromJson(cached, PageResponse.class);
            }
            log.info("未命中置顶列表缓存，查询数据库，key={}", key);
            PageResponse<PostListItemResponse> response = extracted(query, page, size, categoryId, orderBy);
            // 只缓存非空结果，避免缓存空数据
            if (response.total() > 0) {
                stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(response), POST_CACHE_MINUTES, TimeUnit.MINUTES);
                log.info("已缓存置顶列表，key={}, total={}", key, response.total());
            } else {
                log.info("结果为空，不缓存，key={}", key);
            }
            return response;
        }

        String key = POST_LIST + page + ":" + size;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            log.info("命中普通列表缓存，key={}", key);
            return JsonUtil.fromJson(value, PageResponse.class);
        }
        log.info("未命中普通列表缓存，查询数据库，key={}", key);

        PageHelper.startPage(page, size);
        List<Post> posts = postMapper.selectByCondition(
                query.getKeyword(),
                categoryId,
                PostContents.POST_STATUS,
                query.getPinned()
        );
        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        List<PostListItemResponse> records = posts.stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());
        int totalPages = (int) Math.ceil((double) pageInfo.getTotal() / pageInfo.getPageSize());
        PageResponse<PostListItemResponse> response = new PageResponse<>(
                records,
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                totalPages,
                0L
        );
        // 只缓存非空结果，避免缓存空数据
        if (pageInfo.getTotal() > 0) {
            stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(response), POST_CACHE_MINUTES, TimeUnit.MINUTES);
            log.info("已缓存普通列表，key={}, total={}", key, pageInfo.getTotal());
        } else {
            log.info("结果为空，不缓存，key={}", key);
        }
        return response;
    }

    private PageResponse<PostListItemResponse> extracted(PostListDto query, int page, int size, Long categoryId, String orderBy) {
        List<Post> posts = postMapper.selectByCondition(
                query.getKeyword(),
                categoryId,
                PostContents.POST_STATUS,
                query.getPinned()
        );
        
        // 在内存中排序
        posts.sort((p1, p2) -> {
            if (orderBy.contains("like_count")) {
                int cmp = Integer.compare(p2.getLikeCount(), p1.getLikeCount());
                return cmp != 0 ? cmp : Long.compare(p2.getId(), p1.getId());
            } else if (orderBy.contains("view_count")) {
                int cmp = Integer.compare(p2.getViewCount(), p1.getViewCount());
                return cmp != 0 ? cmp : Long.compare(p2.getId(), p1.getId());
            } else if (orderBy.contains("comment_count")) {
                int cmp = Integer.compare(p2.getCommentCount(), p1.getCommentCount());
                return cmp != 0 ? cmp : Long.compare(p2.getId(), p1.getId());
            } else if (orderBy.contains("published_at")) {
                int cmp = p2.getPublishedAt().compareTo(p1.getPublishedAt());
                return cmp != 0 ? cmp : Long.compare(p2.getId(), p1.getId());
            } else {
                // 默认按 ID 降序
                return Long.compare(p2.getId(), p1.getId());
            }
        });
        
        int totalPages = (int) Math.ceil((double) posts.size() / size);
        PageResponse<PostListItemResponse> response = new PageResponse<>(
                posts.stream().map(this::convertToListItem).collect(Collectors.toList()),
                posts.size(),
                page,
                size,
                totalPages,
                0L
        );
        return response;
    }

    /**
     * 函数用途：管理后台查询文章分页列表。
     *
     * @param query 管理后台列表筛选与分页参数
     * @return 文章分页结果
     */
    @Override
    public PageResponse<AdminPostListItemResponse> listAdminPosts(AdminPostListDto query) {
        int page = query.getPage() != null ? query.getPage() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 20;

        // 解析排序参数，格式为 field:direction
        String orderBy = parseSortParameter(query.getSort());

        PageHelper.startPage(page, pageSize);
        List<Post> posts = postMapper.selectAdminList(
                query.getKeyword(),
                query.getStatus(),
                query.getCategory(),
                query.getAuthorId(),
                orderBy
        );
        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        
        List<AdminPostListItemResponse> records = posts.stream()
                .map(this::convertToAdminListItem)
                .collect(Collectors.toList());
        
        int totalPages = (int) Math.ceil((double) pageInfo.getTotal() / pageInfo.getPageSize());
        return new PageResponse<>(
                records,
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                totalPages,
                0L
        );
    }

    /**
     * 函数用途：解析排序参数字符串。
     *
     * @param sort 排序字符串，格式为 field:direction
     * @return SQL ORDER BY 子句
     */
    private String parseSortParameter(String sort) {
        if (sort == null || sort.isEmpty()) {
            return "updated_at DESC, id DESC";
        }
        
        // 验证排序字段，防止SQL注入
        String[] parts = sort.split(":");
        if (parts.length != 2) {
            return "updated_at DESC, id DESC";
        }
        
        String field = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();
        
        // 白名单验证字段名
        Set<String> allowedFields = Set.of("created_at", "updated_at", "published_at", "like_count", "view_count", "comment_count");
        if (!allowedFields.contains(field)) {
            return "updated_at DESC, id DESC";
        }
        
        // 验证排序方向
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            return "updated_at DESC, id DESC";
        }
        
        return field + " " + direction + ", id DESC";
    }
    
    /**
     * 函数用途：解析公开接口的排序参数。
     * 支持简化的排序方式：latest, likes, views, comments
     *
     * @param sort 排序字符串
     * @return 排序表达式（用于内存排序）
     */
    private String parsePublicSortParameter(String sort) {
        if (sort == null || sort.isEmpty()) {
            return "updated_at DESC";
        }
        
        switch (sort.toLowerCase()) {
            case "likes":
                return "like_count DESC";
            case "views":
                return "view_count DESC";
            case "comments":
                return "comment_count DESC";
            case "latest":
            default:
                return "updated_at DESC";
        }
    }

    /**
     * 函数用途：把文章实体转换成列表项响应对象。
     *
     * @param post 文章实体
     * @return 列表项响应
     */
    private PostListItemResponse convertToListItem(Post post) {
        List<String> tags = tagMapper.selectNamesByPostId(post.getId());
        return new PostListItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCoverUrl(),
                post.getCategoryId(),
                tags,
                normalizeCount(post.getLikeCount()),
                normalizeCount(post.getViewCount()),
                Boolean.TRUE.equals(post.getPinned()),
                post.getUpdatedAt()
        );
    }

    /**
     * 函数用途：把文章实体转换成管理后台列表项响应对象。
     *
     * @param post 文章实体
     * @return 管理后台列表项响应
     */
    private AdminPostListItemResponse convertToAdminListItem(Post post) {
        // 获取分类信息
        AdminPostListItemResponse.CategoryInfo categoryInfo = null;
        if (post.getCategoryId() != null) {
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                categoryInfo = new AdminPostListItemResponse.CategoryInfo(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getParentId(),
                        category.getSortOrder(),
                        category.getDescription(),
                        List.of(), // children 暂时为空，如需可递归查询
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                );
            }
        }

        // 获取标签信息
        List<Tag> tags = tagMapper.selectTagsByPostId(post.getId());
        List<AdminPostListItemResponse.TagInfo> tagInfos = tags.stream()
                .map(tag -> new AdminPostListItemResponse.TagInfo(
                        tag.getId(),
                        tag.getName(),
                        tag.getSlug(),
                        tag.getPostCount(),
                        tag.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new AdminPostListItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCoverUrl(),
                categoryInfo,
                tagInfos,
                normalizeCount(post.getLikeCount()),
                normalizeCount(post.getViewCount()),
                normalizeCount(post.getCommentCount()),
                Boolean.TRUE.equals(post.getPinned()) ? 1 : 0,
                post.getStatus(),
                post.getUpdatedAt(),
                post.getCreatedAt()
        );
    }

    /**
     * 函数用途：查询文章详情。
     * 作用说明：详情中的点赞态是用户相关字段，不能直接信任共享缓存，因此会在命中缓存后重新计算。
     *
     * @param postId 文章主键
     * @return 文章详情
     */
    @Override
    public PostDetailResponse getPostDetail(Long postId) {
        Long currentUserId = getCurrentUserId();
        String cacheKey = POST_DETAIL + postId;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            PostDetailResponse cached = JsonUtil.fromJson(cachedJson, PostDetailResponse.class);
            if (cached != null) {
                int likeCount = resolveAuthoritativeLikeCount(postId, cached.likeCount());
                boolean liked = resolveLikedStatus(postId, currentUserId);
                // 从 Redis Hash 获取最新的浏览量
                int viewCount = getViewCountFromCache(postId);
                return rebuildPostDetailResponse(cached, likeCount, viewCount, liked);
            }
        }

        Post post = getExistingPost(postId);
        List<String> tags = tagMapper.selectNamesByPostId(postId);
        int likeCount = resolveAuthoritativeLikeCount(postId, post.getLikeCount());
        boolean liked = resolveLikedStatus(postId, currentUserId);
        
        // 确保 POST_DATA 缓存已初始化，避免后续 increment 操作从1开始
        ensurePostDataCacheExists(postId, post);
        
        PostDetailResponse response = buildPostDetailResponse(post, tags, likeCount, liked);
        cachePostDetailSnapshot(response);
        return response;
    }

    /**
     * 函数用途：执行文章点赞或取消点赞。
     * 作用说明：先在数据库事务中完成点赞关系切换和计数更新，再在事务提交后同步 Redis Hash，
     * 避免 Redis 先成功、数据库后失败导致的双写不一致。
     *
     * @param postId 文章主键
     * @return 点赞后的交互结果
     */
    @Transactional
    @Override
    public PostInteractionResponse likePost(Long postId) {
        Long userId = requireCurrentUserId();
        getExistingPost(postId);

        LikeToggleResult result = toggleLikeInDatabase(postId, userId);
        registerLikeCacheSyncAfterCommit(postId, userId, result.isLiked(), result.getLikeCount());

        log.info("文章点赞切换完成，postId={}, userId={}, liked={}, likeCount={}", 
                postId, userId, result.isLiked(), result.getLikeCount());
        
        // 从 Redis Hash 中获取浏览量，返回完整的统计信息
        int viewCount = getViewCountFromCache(postId);
        return new PostInteractionResponse(postId, result.getLikeCount(), viewCount, result.isLiked());
    }

    /**
     * 函数用途：记录文章浏览。
     * 实现逻辑：
     * 1. 生成用户指纹哈希。
     * 2. 判断 30 分钟内是否已经浏览过。
     * 3. 未命中去重时才增加浏览量。
     *
     * @param postId 文章主键
     * @return 浏览后的交互结果
     */
    @Override
    public PostInteractionResponse recordView(Long postId) {
        Post post = getExistingPost(postId);

        String dedupKeyHash;
        try {
            Long userId = getCurrentUserId();
            String rawKey = userId != null ? "user:" + userId : "anonymous:default";
            dedupKeyHash = sha256(rawKey);
        } catch (Exception e) {
            dedupKeyHash = sha256("anonymous:error");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

        PostViewDedupDaily existingRecord = postViewDedupDailyMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostViewDedupDaily>()
                        .eq(PostViewDedupDaily::getPostId, postId)
                        .eq(PostViewDedupDaily::getDedupDate, today)
                        .eq(PostViewDedupDaily::getDedupKeyHash, dedupKeyHash)
        );

        boolean shouldIncrement = false;
        if (existingRecord == null) {
            shouldIncrement = true;
            PostViewDedupDaily newRecord = PostViewDedupDaily.builder()
                    .postId(postId)
                    .dedupDate(today)
                    .dedupKeyHash(dedupKeyHash)
                    .createdAt(now)
                    .lastViewedAt(now)
                    .build();
            postViewDedupDailyMapper.insert(newRecord);
        } else {
            LocalDateTime lastViewedAt = existingRecord.getLastViewedAt();
            if (lastViewedAt == null || lastViewedAt.isBefore(thirtyMinutesAgo)) {
                shouldIncrement = true;
                existingRecord.setLastViewedAt(now);
                postViewDedupDailyMapper.updateById(existingRecord);
            }
        }

        int viewCount;
        if (shouldIncrement) {
            // 更新 Redis Hash 中的浏览量
            String redisKey = POST_DATA + postId;
            // 先确保 Redis 中有初始值，再执行 increment
            ensurePostDataCacheExists(postId, post);
            // 使用 increment 原子操作增加，并获取最新值
            Long incrementedValue = stringRedisTemplate.opsForHash().increment(redisKey, "viewCount", 1);
            viewCount = incrementedValue != null ? incrementedValue.intValue() : normalizeCount(post.getViewCount()) + 1;
            log.debug("文章浏览量已增加（Redis Hash），postId={}, newViewCount={}", postId, viewCount);
        } else {
            // 即使不增加，也要确保 Redis 中有数据
            ensurePostDataCacheExists(postId, post);
            // 从 Redis 获取最新的浏览量
            viewCount = getViewCountFromCache(postId);
        }

        // 获取点赞数和点赞状态，返回完整的统计信息
        int likeCount = getLikeCountFromCache(postId, post);
        Long currentUserId = getCurrentUserId();
        boolean liked = false;
        if (currentUserId != null) {
            liked = hasUserLikedInDatabase(postId, currentUserId);
        }

        return new PostInteractionResponse(postId, likeCount, viewCount, liked);
    }

    /**
     * 函数用途：获取文章统一统计信息（点赞数、浏览量、点赞状态）。
     * 作用说明：从 Redis Hash 中读取最新数据，并同步到数据库，保证数据一致性。
     *
     * @param postId 文章主键
     * @return 文章统计信息
     */
    @Override
    public PostInteractionResponse getPostStats(Long postId) {
        // 1. 验证文章存在
        Post post = getExistingPost(postId);
        
        // 2. 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        
        // 3. 从 Redis Hash 中获取统计数据
        String redisKey = POST_DATA + postId;
        Map<Object, Object> statsMap = stringRedisTemplate.opsForHash().entries(redisKey);
        
        int likeCount;
        int viewCount;
        
        if (statsMap != null && !statsMap.isEmpty()) {
            // 从 Redis 中读取
            likeCount = statsMap.containsKey("likeCount") ? 
                    Integer.parseInt(statsMap.get("likeCount").toString()) : 
                    normalizeCount(post.getLikeCount());
            viewCount = statsMap.containsKey("viewCount") ? 
                    Integer.parseInt(statsMap.get("viewCount").toString()) : 
                    normalizeCount(post.getViewCount());
        } else {
            // Redis 中没有数据，从数据库初始化
            likeCount = normalizeCount(post.getLikeCount());
            viewCount = normalizeCount(post.getViewCount());
            // 初始化 Redis 缓存
            initPostDataCache(postId, likeCount, viewCount);
        }
        
        // 4. 查询当前用户的点赞状态（从数据库查询）
        boolean liked = false;
        if (currentUserId != null) {
            liked = hasUserLikedInDatabase(postId, currentUserId);
        }
        
        // 注意：不再立即同步到数据库，由定时任务每5分钟批量同步
        // 避免频繁写库和并发覆盖问题
        
        log.debug("获取文章统计信息，postId={}, likeCount={}, viewCount={}, liked={}", 
                postId, likeCount, viewCount, liked);
        
        return new PostInteractionResponse(postId, likeCount, viewCount, liked);
    }

    /**
     * 函数用途：初始化文章数据缓存到 Redis Hash。
     *
     * @param postId 文章ID
     * @param likeCount 点赞数
     * @param viewCount 浏览量
     */
    private void initPostDataCache(Long postId, int likeCount, int viewCount) {
        try {
            String redisKey = POST_DATA + postId;
            Map<String, String> statsMap = new HashMap<>();
            statsMap.put("likeCount", String.valueOf(likeCount));
            statsMap.put("viewCount", String.valueOf(viewCount));
            stringRedisTemplate.opsForHash().putAll(redisKey, statsMap);
            stringRedisTemplate.expire(redisKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);
            log.debug("初始化文章数据缓存成功，postId={}", postId);
        } catch (Exception e) {
            log.warn("初始化文章数据缓存失败，postId={}", postId, e);

        }
    }

    /**
     * 函数用途：确保文章数据缓存存在。
     *
     * @param postId 文章ID
     * @param post 文章实体
     */
    private void ensurePostDataCacheExists(Long postId, Post post) {
        String redisKey = POST_DATA + postId;
        Object viewCountObj = stringRedisTemplate.opsForHash().get(redisKey, "viewCount");
        if (viewCountObj != null) {
            return; // 字段已存在，无需初始化
        }
        // 无论是 key 不存在还是缺少 viewCount 字段，都重新初始化完整数据
        initPostDataCache(postId, normalizeCount(post.getLikeCount()), normalizeCount(post.getViewCount()));
    }

    /**
     * 函数用途：从 Redis Hash 中获取点赞数。
     *
     * @param postId 文章ID
     * @param post 文章实体
     * @return 点赞数
     */
    private int getLikeCountFromCache(Long postId, Post post) {
        String redisKey = POST_DATA + postId;
        Object likeCountObj = stringRedisTemplate.opsForHash().get(redisKey, "likeCount");
        if (likeCountObj != null) {
            return Integer.parseInt(likeCountObj.toString());
        }
        // 如果 Redis 中没有，从数据库获取并初始化缓存
        int likeCount = normalizeCount(post.getLikeCount());
        ensurePostDataCacheExists(postId, post);
        return likeCount;
    }

    /**
     * 函数用途：从 Redis Hash 中获取浏览量。
     *
     * @param postId 文章ID
     * @return 浏览量
     */
    private int getViewCountFromCache(Long postId) {
        String redisKey = POST_DATA + postId;
        Object viewCountObj = stringRedisTemplate.opsForHash().get(redisKey, "viewCount");
        if (viewCountObj != null) {
            return Integer.parseInt(viewCountObj.toString());
        }
        // 如果 Redis 中没有，从数据库获取
        Post post = postMapper.selectById(postId);
        if (post != null) {
            int viewCount = normalizeCount(post.getViewCount());
            ensurePostDataCacheExists(postId, post);
            return viewCount;
        }
        return 0;
    }

    /**
     * 函数用途：将 Redis 中的统计数据同步到数据库。
     * 作用说明：异步更新数据库，确保数据最终一致性。
     *
     * @param postId 文章ID
     * @param likeCount 点赞数
     * @param viewCount 浏览量
     */
    private void syncStatsToDatabase(Long postId, int likeCount, int viewCount) {
        try {
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.set("like_count", likeCount);
            updateWrapper.set("view_count", viewCount);
            postMapper.update(null, updateWrapper);
            log.debug("同步统计数据到数据库成功，postId={}, likeCount={}, viewCount={}", 
                    postId, likeCount, viewCount);
        } catch (Exception e) {
            log.warn("同步统计数据到数据库失败，postId={}", postId, e);
        }
    }

    /**
     * 函数用途：计算字符串的 SHA-256 哈希值。
     *
     * @param input 输入字符串
     * @return SHA-256 哈希值的十六进制表示
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
    @Transactional
    public PostDetailResponse savePost(Long postId, UpsertPostDto request) {
        // 如果未传slug，则根据标题自动生成
        String slug = request.slug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlugFromTitle(request.title());
        }

        // 检查 slug 是否已存在（排除当前文章）
        if (isSlugExists(slug, postId)) {
            // 如果自动生成的slug重复，添加随机后缀
            if (request.slug() == null || request.slug().trim().isEmpty()) {
                slug = slug + "-" + System.currentTimeMillis();
            } else {
                throw new BusinessException(ResultCode.CONFLICT, "文章slug已存在");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Long authorId = getCurrentUserId() != null ? getCurrentUserId() : 1L; // 默认作者ID为1
        Post existingPost = null;

        if (postId == null) {
            // 新增文章
            Post post = new Post();
            post.setTitle(request.title());
            post.setSlug(slug);
            post.setSummary(request.summary());
            post.setCoverUrl(request.coverUrl());
            post.setContentHtml(request.contentHtml());
            post.setTocJson(request.tocJson());
            post.setAuthorId(authorId);
            post.setCategoryId(request.categoryId());
            post.setStatus(request.status());
            post.setPinned(request.isPinned() != null && request.isPinned() == 1);
            // 如果前端传了发布时间则使用，否则根据状态自动设置
            post.setPublishedAt(request.publishedAt() != null ? request.publishedAt() : (request.status() == 1 ? now : null));
            post.setLikeCount(0);
            post.setViewCount(0);
            post.setCommentCount(0);
            post.setCreatedAt(now);
            post.setUpdatedAt(now);

            postMapper.insert(post);
            postId = post.getId();

            log.info("创建文章成功，id={}, title={}, slug={}", postId, request.title(), slug);
        } else {
            // 更新文章
            existingPost = postMapper.selectById(postId);
            if (existingPost == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
            }

            existingPost.setTitle(request.title());
            existingPost.setSlug(slug);
            existingPost.setSummary(request.summary());
            existingPost.setCoverUrl(request.coverUrl());
            existingPost.setContentHtml(request.contentHtml());
            existingPost.setTocJson(request.tocJson());
            existingPost.setCategoryId(request.categoryId());
            existingPost.setStatus(request.status());
            existingPost.setPinned(request.isPinned() != null && request.isPinned() == 1);
            // 如果前端传了发布时间则使用，否则根据状态自动设置
            if (request.publishedAt() != null) {
                existingPost.setPublishedAt(request.publishedAt());
            } else if (request.status() == 1 && existingPost.getPublishedAt() == null) {
                existingPost.setPublishedAt(now);
            }
            existingPost.setUpdatedAt(now);

            postMapper.updateById(existingPost);

            log.info("更新文章成功，id={}, title={}, slug={}", postId, request.title(), slug);
        }

        // 处理标签关联（post_tags表）
        handlePostTags(postId, request.tags());

        // 清除所有文章相关缓存（详情、列表、置顶列表）
        evictAllPostCaches(postId);
        
        // 如果是更新文章，发送通知给互动过的用户
        if (postId != null && existingPost != null) {
            // 异步发送通知，避免阻塞主流程
            try {
                notificationPublisher.notifyPostInteractors(postId, request.title());
            } catch (Exception e) {
                log.error("发送文章更新通知失败，postId={}", postId, e);
            }
        }

        // 返回文章详情
        return getPostDetail(postId);
    }

    /**
     * 函数用途：根据文章标题自动生成slug。
     *
     * @param title 文章标题
     * @return 生成的slug
     */
    private String generateSlugFromTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "post-" + System.currentTimeMillis();
        }

        // 移除首尾空格
        String slug = title.trim();

        // 简单处理：保留字母、数字、中文，其他字符替换为连字符
        slug = slug.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "-");

        // 将连续多个连字符替换为单个
        slug = slug.replaceAll("-+", "-");

        // 去除首尾连字符
        slug = slug.replaceAll("^-+|-+$", "");

        // 转为小写
        slug = slug.toLowerCase();

        // 如果处理后为空，使用时间戳
        if (slug.isEmpty()) {
            slug = "post-" + System.currentTimeMillis();
        }

        // 限制长度
        if (slug.length() > 200) {
            slug = slug.substring(0, 200);
        }

        return slug;
    }

    /**
     * 函数用途：检查slug是否已存在。
     *
     * @param slug 待检查的slug
     * @param excludePostId 排除的文章ID（更新时使用）
     * @return true表示已存在
     */
    private boolean isSlugExists(String slug, Long excludePostId) {
        Post existingPost = postMapper.selectBySlug(slug);
        if (existingPost == null) {
            return false;
        }
        // 如果是更新操作，排除当前文章
        if (excludePostId != null && existingPost.getId().equals(excludePostId)) {
            return false;
        }
        return true;
    }

    /**
     * 函数用途：获取文章点赞状态。
     * 作用说明：点赞数和用户点赞态都以数据库真值为准，同时把结果回填到 Redis。
     *
     * @param postId 文章主键
     * @return 点赞状态
     */
    @Override
    public PostGetLikeResponse getPostLikeStatus(Long postId) {
        Post post = getExistingPost(postId);
        int likeCount = resolveAuthoritativeLikeCount(postId, post.getLikeCount());
//        boolean liked = resolveLikedStatus(postId, getCurrentUserId());
//        int likeCount = post.getLikeCount();
        boolean liked = resolveLikedStatus(postId, getCurrentUserId());
        log.info("获取文章点赞状态，postId={}, liked={}, likeCount={}", postId, liked, likeCount);
        return new PostGetLikeResponse(likeCount, liked);
    }

    /**
     * 函数用途：根据主键获取未删除的文章。
     *
     * @param postId 文章ID
     * @return 文章实体
     */
    private Post getExistingPost(Long postId) {
        Post post = postMapper.selectByIdAndDeletedAtIsNull(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在或已删除");
        }
        return post;
    }

    /**
     * 函数用途：获取当前登录用户ID。
     *
     * @return 登录用户ID，未登录时返回 null
     */
    private Long getCurrentUserId() {
        UserDto user = UserHolder.getUserId();
        return user != null ? user.getId() : null;
    }

    /**
     * 函数用途：获取当前登录用户ID并校验登录态。
     *
     * @return 登录用户ID
     */
    private Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }

    /**
     * 函数用途：切换数据库中的点赞关系。
     * 作用说明：只处理数据库真值，不直接写 Redis，保证事务边界清晰。
     *
     * @param postId 文章ID
     * @param userId 用户ID
     * @return 包含点赞状态和最新点赞数的结果
     */
    private LikeToggleResult toggleLikeInDatabase(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        int currentLikeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        
        if (hasUserLikedInDatabase(postId, userId)) {
            // 已点赞，执行取消点赞
            int deleteResult = postLikeMapper.deleteByUserIdAndPostId(userId, postId);
            if (deleteResult > 0) {
                // 删除成功，点赞数减1
                UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", postId);
                updateWrapper.setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
                postMapper.update(null, updateWrapper);
                currentLikeCount = Math.max(currentLikeCount - 1, 0);
            }
            return new LikeToggleResult(false, currentLikeCount);
        }

        // 未点赞，执行点赞
        try {
            postLikeMapper.insert(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build());
            
            // 插入成功，点赞数加1
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.setSql("like_count = like_count + 1");
            postMapper.update(null, updateWrapper);
            currentLikeCount = currentLikeCount + 1;
            
            return new LikeToggleResult(true, currentLikeCount);
        } catch (DuplicateKeyException ex) {
            // 并发冲突，说明已经点赞过
            log.debug("并发点赞命中唯一约束，postId={}, userId={}", postId, userId);
            boolean liked = hasUserLikedInDatabase(postId, userId);
            return new LikeToggleResult(liked, currentLikeCount);
        }
    }
    
    /**
     * 内部类：点赞切换结果
     */
    private static class LikeToggleResult {
        private final boolean liked;
        private final int likeCount;
        
        public LikeToggleResult(boolean liked, int likeCount) {
            this.liked = liked;
            this.likeCount = likeCount;
        }
        
        public boolean isLiked() {
            return liked;
        }
        
        public int getLikeCount() {
            return likeCount;
        }
    }

    /**
     * 函数用途：判断用户是否点赞过指定文章。
     *
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true 表示已点赞
     */
    private boolean hasUserLikedInDatabase(Long postId, Long userId) {
        // 1. 先查 Redis Set 缓存（已点赞用户）
        String likeUserKey = POST_LIKE_USER + postId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(likeUserKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(isMember)) {
            return true;
        }
        
        // 2. 再查未点赞状态缓存（避免缓存穿透）
        String notLikedKey = POST_LIKE_NOT + postId + ":" + userId;
        Boolean hasNotLikedCache = stringRedisTemplate.hasKey(notLikedKey);
        if (Boolean.TRUE.equals(hasNotLikedCache)) {
            return false;  // 缓存了未点赞状态
        }
        
        // 3. Redis 中都没有，查数据库
        boolean liked = postLikeMapper.countByUserIdAndPostId(userId, postId) > 0;
        
        // 4. 同步到 Redis
        if (liked) {
            // 已点赞：添加到 Set，长期缓存
            stringRedisTemplate.opsForSet().add(likeUserKey, String.valueOf(userId));
            stringRedisTemplate.expire(likeUserKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);
        } else {
            // 未点赞：缓存未点赞状态，短期有效（避免频繁查库）
            stringRedisTemplate.opsForValue().set(notLikedKey, "0", NOT_LIKED_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        
        return liked;
    }

    /**
     * 函数用途：获取文章的权威点赞数。
     * 作用说明：优先相信主表计数字段，定期以 post_likes 明细表为准做校准。
     *
     * @param postId 文章ID
     * @param storedLikeCount 主表中当前保存的点赞数
     * @return 权威点赞数
     */
    private int resolveAuthoritativeLikeCount(Long postId, Integer storedLikeCount) {
        // 如果主表有值，直接返回（增量更新已保证准确性）
        if (storedLikeCount != null && storedLikeCount >= 0) {
            // 仅当 POST_DATA 缓存已存在时同步 likeCount，避免创建残缺缓存
            //（缺少 viewCount 会导致后续 ensurePostDataCacheExists 误判为已初始化，
            //  进而使 recordView 的 increment 从 0 开始，把浏览量错误地变为 1）
            String redisKey = POST_DATA + postId;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                stringRedisTemplate.opsForHash().put(redisKey, "likeCount", String.valueOf(storedLikeCount));
                stringRedisTemplate.expire(redisKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);
            }
            return storedLikeCount;
        }

        // 主表为空或异常时，才从明细表统计并修复
        log.warn("文章点赞计数异常，尝试从明细表修复，postId={}, storedLikeCount={}",
                postId, storedLikeCount);
        int actualLikeCount = postLikeMapper.countByPostId(postId);
        persistLikeCountSnapshot(postId, actualLikeCount);
        // 仅当缓存已存在时同步
        String redisKey = POST_DATA + postId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            stringRedisTemplate.opsForHash().put(redisKey, "likeCount", String.valueOf(actualLikeCount));
            stringRedisTemplate.expire(redisKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        return actualLikeCount;
    }



    /**
     * 函数用途：把真实点赞数写回文章主表。
     *
     * @param postId 文章ID
     * @param likeCount 真实点赞数
     */
    private void persistLikeCountSnapshot(Long postId, int likeCount) {
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId);
        updateWrapper.set("like_count", likeCount);
        postMapper.update(null, updateWrapper);
    }

    /**
     * 函数用途：解析当前用户的点赞状态。
     * 作用说明：数据库结果作为最终真值，Redis 只做缓存同步，避免读到脏状态。
     *
     * @param postId 文章ID
     * @param userId 用户ID
     * @return true 表示已点赞
     */
    private boolean resolveLikedStatus(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }

        boolean liked = hasUserLikedInDatabase(postId, userId);
        // syncLikeMemberCache 已废弃，不再调用
        return liked;
    }


    /**
     * 函数用途：在事务提交后同步点赞缓存。
     * 作用说明：只有数据库成功提交后才允许刷新 Redis，避免缓存提前写入造成脏数据。
     *
     * @param postId 文章ID
     * @param userId 用户ID
     * @param liked 最新点赞状态
     * @param likeCount 最新点赞数
     */
    private void registerLikeCacheSyncAfterCommit(Long postId, Long userId, boolean liked, int likeCount) {
        Runnable syncTask = () -> syncLikeCacheAfterCommit(postId, userId, liked, likeCount);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    syncTask.run();
                }
            });
            return;
        }
        syncTask.run();
    }

    /**
     * 函数用途：在事务提交成功后刷新 Redis。
     *
     * @param postId 文章ID
     * @param userId 用户ID
     * @param liked 最新点赞状态
     * @param likeCount 最新点赞数
     */
    private void syncLikeCacheAfterCommit(Long postId, Long userId, boolean liked, int likeCount) {
        // 更新 Redis Hash 中的点赞数
        String redisKey = POST_DATA + postId;
        stringRedisTemplate.opsForHash().put(redisKey, "likeCount", String.valueOf(likeCount));
        stringRedisTemplate.expire(redisKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);
        
        // 更新 Redis Set 中的点赞用户
        String likeUserKey = POST_LIKE_USER + postId;
        if (liked) {
            // 用户点赞，添加到 Set
            stringRedisTemplate.opsForSet().add(likeUserKey, String.valueOf(userId));
            // 清除未点赞缓存（如果存在）
            String notLikedKey = POST_LIKE_NOT + postId + ":" + userId;
            stringRedisTemplate.delete(notLikedKey);
        } else {
            // 用户取消点赞，从 Set 中移除
            stringRedisTemplate.opsForSet().remove(likeUserKey, String.valueOf(userId));
            // 设置未点赞缓存
            String notLikedKey = POST_LIKE_NOT + postId + ":" + userId;
            stringRedisTemplate.opsForValue().set(notLikedKey, "0", NOT_LIKED_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        stringRedisTemplate.expire(likeUserKey, POST_CACHE_MINUTES, TimeUnit.MINUTES);

        evictLikeRelatedCaches(postId);
    }

    /**
     * 函数用途：清理点赞相关的文章缓存。
     * 作用说明：文章详情和文章列表都包含点赞数，点赞变化后必须失效。
     *
     * @param postId 文章ID
     */
    private void evictLikeRelatedCaches(Long postId) {
        try {
            stringRedisTemplate.delete(POST_DETAIL + postId);
            evictCacheByPattern(POST_LIST + "*");
            evictCacheByPattern(POST_LIST_PINNED + "*");
        } catch (Exception ex) {
            log.warn("清理文章点赞相关缓存失败，postId={}", postId, ex);
        }
    }

    /**
     * 函数用途：清除所有文章相关缓存。
     * 作用说明：文章创建或更新后，需要清除所有缓存以保证数据一致性。
     *
     * @param postId 文章ID
     */
    private void evictAllPostCaches(Long postId) {
        try {
            // 清除当前文章详情缓存
            stringRedisTemplate.delete(POST_DETAIL + postId);
            
            // 清除所有分页的普通列表缓存
            evictCacheByPattern(POST_LIST + "*");
            
            // 清除所有分页的置顶列表缓存
            evictCacheByPattern(POST_LIST_PINNED + "*");
            
            log.info("已清除文章相关缓存，postId={}", postId);
        } catch (Exception ex) {
            log.warn("清除文章缓存失败，postId={}", postId, ex);
        }
    }

    /**
     * 函数用途：按前缀删除缓存。
     * 作用说明：当前项目体量较小，列表缓存按模式清理可以接受。
     *
     * @param pattern Redis key 模式
     */
    private void evictCacheByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * 函数用途：构建文章详情响应对象。
     *
     * @param post 文章实体
     * @param tags 标签列表
     * @param likeCount 权威点赞数
     * @param liked 当前用户点赞态
     * @return 文章详情响应
     */
    private PostDetailResponse buildPostDetailResponse(Post post, List<String> tags, int likeCount, boolean liked) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCoverUrl(),
                post.getContentHtml(),
                post.getTocJson(),
                post.getAuthorId(),
                post.getCategoryId(),
                tags,
                likeCount,
                normalizeCount(post.getViewCount()),
                normalizeCount(post.getCommentCount()),
                liked,
                Boolean.TRUE.equals(post.getPinned()),
                post.getPublishedAt(),
                post.getUpdatedAt()
        );
    }

    /**
     * 函数用途：基于缓存快照重建文章详情响应。
     * 作用说明：缓存只保存公共字段，请求返回前会覆盖成当前用户真实的点赞状态和浏览量。
     *
     * @param cached 缓存中的详情快照
     * @param likeCount 权威点赞数
     * @param viewCount 最新浏览量
     * @param liked 当前用户点赞态
     * @return 修正后的文章详情响应
     */
    private PostDetailResponse rebuildPostDetailResponse(PostDetailResponse cached, int likeCount, int viewCount, boolean liked) {
        return new PostDetailResponse(
                cached.id(),
                cached.title(),
                cached.summary(),
                cached.coverUrl(),
                cached.contentHtml(),
                cached.tocJson(),
                cached.authorId(),
                cached.categoryId(),
                cached.tags(),
                likeCount,
                viewCount,
                cached.commentCount(),
                liked,
                cached.pinned(),
                cached.publishedAt(),
                cached.updatedAt()
        );
    }

    /**
     * 函数用途：缓存文章详情快照。
     * 作用说明：缓存中不保存用户态的 `liked=true`，避免不同用户之间串数据。
     *
     * @param response 文章详情响应
     */
    private void cachePostDetailSnapshot(PostDetailResponse response) {
        PostDetailResponse cacheValue = new PostDetailResponse(
                response.id(),
                response.title(),
                response.summary(),
                response.coverUrl(),
                response.contentHtml(),
                response.tocJson(),
                response.authorId(),
                response.categoryId(),
                response.tags(),
                response.likeCount(),
                response.viewCount(),
                response.commentCount(),
                false,
                response.pinned(),
                response.publishedAt(),
                response.updatedAt()
        );
        stringRedisTemplate.opsForValue().set(
                POST_DETAIL + response.id(),
                JsonUtil.toJson(cacheValue),
                POST_CACHE_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * 函数用途：把可能为 null 的计数字段转为安全整数。
     *
     * @param count 原始计数
     * @return 非空计数
     */
    private int normalizeCount(Integer count) {
        return count != null ? count : 0;
    }

    /**
     * 函数用途：处理文章标签关联。
     *
     * @param postId 文章ID
     * @param tagNames 标签名称列表
     */
    private void handlePostTags(Long postId, List<String> tagNames) {
        if (tagNames == null) {
            tagNames = List.of(); // 默认为空列表
        }

        // 1. 删除旧的标签关联
        postTagMapper.deleteByPostId(postId);

        // 2. 处理新的标签
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }

            String trimmedName = tagName.trim();

            // 查找或创建标签
            Tag tag = tagMapper.selectByName(trimmedName);
            if (tag == null) {
                // 创建新标签
                LocalDateTime now = LocalDateTime.now();
                tag = new Tag();
                tag.setName(trimmedName);
                tag.setSlug(generateSlugFromTitle(trimmedName)); // 根据名称生成 slug
                tag.setPostCount(0);
                tag.setCreatedAt(now);
                tagMapper.insert(tag);
                log.info("创建新标签，name={}, id={}", trimmedName, tag.getId());
            }

            // 创建文章-标签关联
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tag.getId());
            postTagMapper.insert(postTag);

            // 更新标签的文章数量
            UpdateWrapper<Tag> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", tag.getId());
            updateWrapper.setSql("post_count = post_count + 1");
            tagMapper.update(null, updateWrapper);
        }

        log.info("处理文章标签关联完成，postId={}, tags={}", postId, tagNames);
    }

    /**
     * 函数用途：管理后台查询文章详情（包含分类和标签的完整信息）。
     *
     * @param postId 文章主键
     * @return 文章详情响应
     */
    @Override
    public AdminPostDetailResponse getAdminPostDetail(Long postId) {
        // 1. 查询文章实体
        Post post = getExistingPost(postId);

        // 2. 获取分类信息
        AdminPostDetailResponse.CategoryInfo categoryInfo = null;
        if (post.getCategoryId() != null) {
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                categoryInfo = new AdminPostDetailResponse.CategoryInfo(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getParentId(),
                        category.getSortOrder(),
                        category.getDescription(),
                        List.of(), // children 暂时为空，如需可递归查询
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                );
            }
        }

        // 3. 获取标签详细信息
        List<Tag> tags = tagMapper.selectTagsByPostId(postId);
        List<AdminPostDetailResponse.TagInfo> tagInfos = tags.stream()
                .map(tag -> new AdminPostDetailResponse.TagInfo(
                        tag.getId(),
                        tag.getName(),
                        tag.getSlug(),
                        tag.getPostCount() != null ? tag.getPostCount() : 0,
                        tag.getCreatedAt()
                ))
                .collect(Collectors.toList());

        // 4. 构建响应对象
        return new AdminPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCoverUrl(),
                post.getContentHtml(),
                post.getTocJson(),
                post.getAuthorId(),
                categoryInfo,
                tagInfos,
                normalizeCount(post.getLikeCount()),
                normalizeCount(post.getViewCount()),
                normalizeCount(post.getCommentCount()),
                Boolean.TRUE.equals(post.getPinned()) ? 1 : 0,
                post.getStatus(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    /**
     * 函数用途：将 Redis 中的浏览量数据同步到数据库。
     * 作用说明：定时任务调用，批量更新所有文章的浏览量。
     */
    @Override
    public void syncViewCountToDatabase() {
        log.info("开始同步文章统计数据到数据库...");
        
        try {
            // 获取所有有统计数据缓存的 key
            Set<String> postDataKeys = stringRedisTemplate.keys(POST_DATA + "*");
            
            if (postDataKeys == null || postDataKeys.isEmpty()) {
                log.info("没有需要同步的统计数据");
                return;
            }
            
            int syncCount = 0;
            for (String key : postDataKeys) {
                try {
                    // 从 key 中提取 postId
                    String postIdStr = key.replace(POST_DATA, "");
                    Long postId = Long.parseLong(postIdStr);
                    
                    // 获取 Redis Hash 中的统计数据
                    Object viewCountObj = stringRedisTemplate.opsForHash().get(key, "viewCount");
                    Object likeCountObj = stringRedisTemplate.opsForHash().get(key, "likeCount");
                    
                    if (viewCountObj != null || likeCountObj != null) {
                        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("id", postId);
                        
                        if (viewCountObj != null) {
                            int redisViewCount = Integer.parseInt(viewCountObj.toString());
                            updateWrapper.set("view_count", redisViewCount);
                            log.debug("同步文章浏览量，postId={}, viewCount={}", postId, redisViewCount);
                        }
                        
                        if (likeCountObj != null) {
                            int redisLikeCount = Integer.parseInt(likeCountObj.toString());
                            updateWrapper.set("like_count", redisLikeCount);
                            log.debug("同步文章点赞数，postId={}, likeCount={}", postId, redisLikeCount);
                        }
                        
                        postMapper.update(null, updateWrapper);
                        syncCount++;
                    }
                } catch (Exception e) {
                    log.warn("同步单个文章统计数据失败，key={}", key, e);
                }
            }
            
            log.info("文章统计数据同步完成，共同步 {} 篇文章", syncCount);
        } catch (Exception e) {
            log.error("同步文章统计数据到数据库失败", e);
        }
    }

    /**
     * 函数用途：将 Redis Set 中的点赞用户同步到数据库。
     * 作用说明：每天凌晨2点执行，防止 Redis 宕机导致数据丢失。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncLikeUsersToDatabase() {
        log.info("开始同步点赞用户数据到数据库...");
        
        try {
            // 获取所有点赞用户集合的 key
            Set<String> likeUserKeys = stringRedisTemplate.keys(POST_LIKE_USER + "*");
            
            if (likeUserKeys == null || likeUserKeys.isEmpty()) {
                log.info("没有需要同步的点赞用户数据");
                return;
            }
            
            int syncCount = 0;
            for (String key : likeUserKeys) {
                try {
                    // 从 key 中提取 postId
                    String postIdStr = key.replace(POST_LIKE_USER, "");
                    Long postId = Long.parseLong(postIdStr);
                    
                    // 获取 Redis Set 中的所有用户ID
                    Set<String> userIds = stringRedisTemplate.opsForSet().members(key);
                    
                    if (userIds == null || userIds.isEmpty()) {
                        continue;
                    }
                    
                    // 先删除该文章的所有点赞记录
                    postLikeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getPostId, postId));
                    
                    // 批量插入新的点赞记录
                    LocalDateTime now = LocalDateTime.now();
                    for (String userIdStr : userIds) {
                        try {
                            Long userId = Long.parseLong(userIdStr);
                            PostLike postLike = PostLike.builder()
                                    .postId(postId)
                                    .userId(userId)
                                    .createdAt(now)
                                    .build();
                            postLikeMapper.insert(postLike);
                        } catch (NumberFormatException e) {
                            log.warn("无效的用户ID格式，userIdStr={}", userIdStr);
                        }
                    }
                    
                    // 更新文章的点赞数
                    int likeCount = userIds.size();
                    UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", postId);
                    updateWrapper.set("like_count", likeCount);
                    postMapper.update(null, updateWrapper);
                    
                    syncCount++;
                    log.debug("同步文章点赞用户成功，postId={}, likeCount={}", postId, likeCount);
                } catch (Exception e) {
                    log.warn("同步单个文章点赞用户失败，key={}", key, e);
                }
            }
            
            log.info("点赞用户数据同步完成，共同步 {} 篇文章", syncCount);
        } catch (Exception e) {
            log.error("同步点赞用户数据到数据库失败", e);
        }
    }

    /**
     * 函数用途：清理30天前的浏览去重记录。
     * 作用说明：每天凌晨3点执行，删除30天前的 post_view_dedup_daily 数据，避免表数据无限增长。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldViewDedupRecords() {
        log.info("开始清理30天前的浏览去重记录...");
        
        try {
            // 计算30天前的日期
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            
            // 删除30天前的记录
            int deletedCount = postViewDedupDailyMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostViewDedupDaily>()
                            .lt(PostViewDedupDaily::getDedupDate, thirtyDaysAgo)
            );
            
            log.info("浏览去重记录清理完成，删除了 {} 条记录（{} 天前的数据）", deletedCount, thirtyDaysAgo);
        } catch (Exception e) {
            log.error("清理浏览去重记录失败", e);
        }
    }

    /**
     * 函数用途：管理后台删除文章。
     *
     * @param postId 文章主键
     */
    @Override
    @Transactional
    public void deletePost(Long postId) {
        // 检查文章是否存在（包括已删除的）
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        
        // 如果已经删除，直接返回
        if (post.getDeletedAt() != null) {
            log.warn("文章已被删除，id={}", postId);
            return;
        }

        // 删除文章的标签关联
        postTagMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));
        
        // 删除文章的点赞记录
        postLikeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId));
        
        // 删除文章的评论
        // TODO: 如果有评论表，需要在这里删除相关评论
        
        // 删除文章的浏览去重记录
        postViewDedupDailyMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostViewDedupDaily>()
                .eq(PostViewDedupDaily::getPostId, postId));
        
        // 删除 Redis 缓存
        String postDataKey = POST_DATA + postId;
        String postDetailKey = POST_DETAIL + postId;
        String postLikeUserKey = POST_LIKE_USER + postId;
        stringRedisTemplate.delete(postDataKey);
        stringRedisTemplate.delete(postDetailKey);
        stringRedisTemplate.delete(postLikeUserKey);
        
        // 清除列表缓存
        evictCacheByPattern(POST_LIST + "*");
        evictCacheByPattern(POST_LIST_PINNED + "*");
        
        // 执行物理删除（直接从数据库删除）
        int deleteResult = postMapper.deleteById(postId);
        
        if (deleteResult <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除文章失败");
        }
        
        log.info("删除文章成功，id={}, title={}", postId, post.getTitle());
    }
}
