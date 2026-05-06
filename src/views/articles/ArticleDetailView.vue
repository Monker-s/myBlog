<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import ArticleToc from "@/components/articles/ArticleToc.vue";
import LikeButton from "@/components/articles/LikeButton.vue";
import CommentForm from "@/components/comments/CommentForm.vue";
import CommentList from "@/components/comments/CommentList.vue";
import {
  createPostComment,
  replyComment,
  getPostComments,
} from "@/api/comments";
import {
  getPostDetail,
  getPostLikeStatus,
  likePost,
  reportPostView,
} from "@/api/posts";
import { unwrapData } from "@/utils/service";
import { sanitizeHtml } from "@/utils/sanitize";
import { formatDateTime } from "@/utils/date";
import { setPageMeta } from "@/utils/seo";
import { ROUTE_NAMES } from "@/constants/routes";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const liking = ref(false);
const liked = ref(false);
const likeCount = ref(0);
const viewCount = ref(0);
const article = ref(null);
const comments = ref([]);
const lastCommentTime = ref(null); // 最后一条评论的时间戳，用于滚动加载
const hasMoreComments = ref(true); // 是否还有更多评论
const loadingComments = ref(false); // 评论加载中
const lastLikeTime = ref(0); // 记录上次点赞时间

// 回复相关状态
const replyTarget = ref(null); // 当前正在回复的评论

// 点赞状态持久化
const LIKE_STORAGE_KEY = "article_likes";
function getSavedLikes() {
  try {
    return JSON.parse(localStorage.getItem(LIKE_STORAGE_KEY)) || {};
  } catch {
    return {};
  }
}
function saveLikeStatus(postId, isLiked) {
  try {
    const likes = getSavedLikes();
    if (isLiked) {
      likes[postId] = true;
    } else {
      delete likes[postId];
    }
    localStorage.setItem(LIKE_STORAGE_KEY, JSON.stringify(likes));
  } catch {
    // 忽略存储错误
  }
}
function isPostLiked(postId) {
  return !!getSavedLikes()[postId];
}

const goBack = () => {
  router.push({ name: ROUTE_NAMES.ARTICLES });
};

const safeHtml = computed(() =>
  sanitizeHtml(article.value?.contentHtml || article.value?.content_html || ""),
);
const toc = computed(() => {
  const raw = article.value?.tocJson || article.value?.toc_json;
  if (typeof raw === "string") {
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }
  return Array.isArray(raw) ? raw : [];
});
const coverUrl = computed(
  () =>
    article.value?.coverUrl ||
    article.value?.cover_url ||
    "/images/ultra-helmet.png",
);

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await getPostDetail(route.params.id);
    console.log("[fetchDetail] 后端返回原始响应:", res);

    const data = unwrapData(res);
    console.log("[fetchDetail] unwrapData 后的数据:", data);

    article.value = data;
    likeCount.value = Number(data?.likeCount ?? data?.like_count ?? 0);
    viewCount.value = Number(data?.viewCount ?? data?.view_count ?? 0);

    console.log("[fetchDetail] 解析后的值:", {
      likeCount: likeCount.value,
      viewCount: viewCount.value,
      raw_liked: data?.liked,
      raw_isLiked: data?.isLiked,
    });

    // 获取点赞状态（优先使用后端单独的接口）
    try {
      const likeRes = await getPostLikeStatus(route.params.id);
      const likeData = unwrapData(likeRes);
      console.log("[fetchDetail] 点赞状态接口返回:", likeData);

      // 后端返回的点赞状态字段可能是 liked, isLiked, 或 likedByCurrentUser
      const backendLiked =
        likeData?.liked ??
        likeData?.isLiked ??
        likeData?.likedByCurrentUser ??
        data?.liked ??
        data?.isLiked;

      console.log("[fetchDetail] 最终 backendLiked 值:", backendLiked);

      if (backendLiked !== undefined && backendLiked !== null) {
        liked.value = Boolean(backendLiked);
        console.log("[fetchDetail] 使用后端点赞状态:", liked.value);
        // 同步到本地存储
        saveLikeStatus(route.params.id, liked.value);
      } else {
        // 后端没返回时使用本地存储的状态
        liked.value = isPostLiked(route.params.id);
        console.log("[fetchDetail] 使用本地存储的点赞状态:", liked.value);
      }
    } catch (likeError) {
      console.warn("[fetchDetail] 获取点赞状态失败，使用本地存储:", likeError);
      liked.value = isPostLiked(route.params.id);
    }

    console.log("[fetchDetail] 最终渲染状态:", {
      liked: liked.value,
      likeCount: likeCount.value,
    });
  } catch (error) {
    console.error("[fetchDetail] 错误:", error);
    ElMessage.error(error?.message || "获取文章详情失败");
    article.value = null;
  } finally {
    loading.value = false;
  }
};

const fetchComments = async (isLoadMore = false) => {
  if (loadingComments.value) return;

  console.log(
    "[fetchComments] 开始执行 isLoadMore:",
    isLoadMore,
    "lastCommentTime:",
    lastCommentTime.value,
  );

  loadingComments.value = true;
  try {
    const params = { pageSize: 20 };
    // 仅加载更多时传 lastCommentTime
    if (isLoadMore && lastCommentTime.value) {
      params.lastCommentTime = lastCommentTime.value;
    }

    console.log(
      "[fetchComments] 请求参数:",
      JSON.stringify(params),
      isLoadMore ? "(加载更多)" : "(首次加载)",
    );

    const res = await getPostComments(route.params.id, params);
    const data = unwrapData(res);
    const newComments = Array.isArray(data)
      ? data
      : data?.list || data?.records || [];

    console.log("[fetchComments] 后端返回数量:", newComments.length);

    if (isLoadMore) {
      // 滚动加载：追加新数据
      comments.value = [...comments.value, ...newComments];
    } else {
      // 首次加载：替换数据
      comments.value = newComments;
    }

    // 更新 lastCommentTime 为最后一条评论的时间（数组最后一个是时间最旧的）
    if (newComments.length > 0) {
      const lastComment = newComments[newComments.length - 1];
      lastCommentTime.value = lastComment.created_at || lastComment.createdAt;
      // 只要有数据，就认为可能还有更多（通过下次请求是否返回空数组来判断）
      hasMoreComments.value = true;
    } else {
      hasMoreComments.value = false;
    }

    console.log(
      "[fetchComments]",
      isLoadMore ? "加载更多" : "首次加载",
      "数量:",
      newComments.length,
      "hasMore:",
      hasMoreComments.value,
      "lastCommentTime:",
      lastCommentTime.value,
    );
  } catch (error) {
    console.error("[fetchComments] 错误:", error);
    if (!isLoadMore) {
      comments.value = [];
    }
    hasMoreComments.value = false;
  } finally {
    loadingComments.value = false;
  }
};

// 加载更多评论
const loadMoreComments = async () => {
  if (!hasMoreComments.value || loadingComments.value) return;
  await fetchComments(true);
};

/**
 * 仅刷新点赞状态，不触发全屏 loading
 */
const refreshLikeStatus = async () => {
  try {
    const res = await getPostDetail(route.params.id);
    const data = unwrapData(res);

    if (data) {
      likeCount.value = Number(
        data?.likeCount ?? data?.like_count ?? likeCount.value,
      );
      viewCount.value = Number(
        data?.viewCount ?? data?.view_count ?? viewCount.value,
      );
      liked.value = Boolean(data?.liked ?? data?.isLiked);
      if (article.value) {
        article.value.likeCount = likeCount.value;
        article.value.viewCount = viewCount.value;
      }
    }
  } catch (error) {
    console.error("❌ refreshLikeStatus 失败:", error);
  }
};

const handleLike = async () => {
  console.log("[handleLike] 开始执行, 当前状态:", {
    liked: liked.value,
    likeCount: likeCount.value,
  });

  // 检查冷却时间（3秒）
  const now = Date.now();
  const timeSinceLastLike = now - lastLikeTime.value;
  if (timeSinceLastLike < 3000) {
    const remainingTime = Math.ceil((3000 - timeSinceLastLike) / 1000);
    ElMessage.warning(`请等待 ${remainingTime} 秒后再操作`);
    return;
  }

  const previousLiked = liked.value;
  const previousCount = likeCount.value;

  // 乐观更新
  liked.value = !previousLiked;
  likeCount.value += previousLiked ? -1 : 1;

  console.log("[handleLike] 乐观更新后:", {
    liked: liked.value,
    likeCount: likeCount.value,
  });

  liking.value = true;
  lastLikeTime.value = now;

  try {
    const res = await likePost(route.params.id);
    console.log("[handleLike] 后端返回原始响应:", res);

    const data = unwrapData(res);
    console.log("[handleLike] unwrapData 后的数据:", data);

    if (data) {
      // 使用后端返回的数据更新状态
      likeCount.value = Number(
        data?.likeCount ?? data?.like_count ?? likeCount.value,
      );
      liked.value = Boolean(data?.liked ?? data?.isLiked);

      // 保存点赞状态到本地存储
      saveLikeStatus(route.params.id, liked.value);

      console.log("[handleLike] 使用后端数据更新:", {
        liked: liked.value,
        likeCount: likeCount.value,
      });

      if (article.value) {
        article.value.likeCount = likeCount.value;
        article.value.viewCount = viewCount.value;
      }
    } else {
      console.log("[handleLike] 后端未返回数据，刷新状态");
      // 如果没有返回数据，再刷新一次状态
      await refreshLikeStatus();
    }
  } catch (error) {
    console.error("[handleLike] 错误:", error);
    // 失败则回滚
    liked.value = previousLiked;
    likeCount.value = previousCount;
    saveLikeStatus(route.params.id, previousLiked);
    console.log("[handleLike] 回滚到:", {
      liked: liked.value,
      likeCount: likeCount.value,
    });
    ElMessage.error(error?.message || "操作失败，请稍后重试");
  } finally {
    liking.value = false;
  }
};

const handleSubmitComment = async (payload) => {
  try {
    // 判断是否为回复模式
    if (replyTarget.value) {
      // 回复评论
      const res = await replyComment(replyTarget.value.id, payload);
      const created = unwrapData(res);

      // 找到父级评论并追加回复
      const parentComment = comments.value.find(
        (c) => c.id === replyTarget.value.id,
      );
      if (parentComment) {
        // 确保 replies 数组存在
        if (!parentComment.replies) {
          parentComment.replies = [];
        }
        parentComment.replies.push(created);
      }

      // 退出回复模式
      replyTarget.value = null;
      ElMessage.success("回复成功");
    } else {
      // 发布新评论
      const res = await createPostComment(route.params.id, payload);
      const created = unwrapData(res);
      comments.value = [created, ...comments.value];
      // 更新 lastCommentTime 为新评论的时间
      lastCommentTime.value = created.created_at || created.createdAt;
      hasMoreComments.value = true; // 新评论后可能还有更多
      ElMessage.success("评论已发布");
    }
  } catch {
    ElMessage.error(replyTarget.value ? "回复失败" : "评论发布失败");
  }
};

// 取消回复
const handleCancelReply = () => {
  replyTarget.value = null;
};

// 触发回复
const handleReply = (comment) => {
  replyTarget.value = comment;
  // 滚动到评论表单
  setTimeout(() => {
    const formEl = document.querySelector(".comment-form");
    if (formEl) {
      formEl.scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }, 100);
};

onMounted(async () => {
  await Promise.all([fetchDetail(), fetchComments()]);

  if (article.value) {
    setPageMeta({
      title: `${article.value?.title || "文章详情"} | 个人知识库`,
      description: "战术文章详情页，包含目录、正文、点赞和评论。",
    });
  }

  // 上报浏览量并刷新显示
  try {
    const viewRes = await reportPostView(route.params.id);
    const viewData = unwrapData(viewRes);
    if (viewData) {
      viewCount.value = Number(
        viewData.viewCount ?? viewData.view_count ?? viewCount.value + 1,
      );
    }
  } catch {
    // 上报失败不影响页面展示
  }
});

// 监听路由参数变化（如不同文章 ID），重置评论状态
watch(
  () => route.params.id,
  (newId, oldId) => {
    // 只在文章 ID 变化时重置（不是首次挂载）
    if (newId !== oldId) {
      console.log(
        "[ArticleDetailView] 路由变化，重置评论状态:",
        oldId,
        "->",
        newId,
      );
      lastCommentTime.value = null;
      comments.value = [];
      hasMoreComments.value = true;
      fetchComments(false); // 明确传 false，表示首次加载
      fetchDetail();
    }
  },
);

onBeforeUnmount(() => {
  // 组件卸载时保存点赞状态
  if (article.value) {
    saveLikeStatus(route.params.id, liked.value);
  }
  // 重置评论状态
  lastCommentTime.value = null;
  comments.value = [];
  hasMoreComments.value = true;
});
</script>

<template>
  <section class="detail-view">
    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else>
      <article class="page-panel article-main">
        <div class="back-bar">
          <el-button class="back-btn" @click="goBack"> ← 返回文献库 </el-button>
        </div>
        <header class="hero-head">
          <img :src="coverUrl" alt="文章主视觉" class="hero-image" />
          <div class="hero-info">
            <p class="signal">TACTICAL DOSSIER</p>
            <h1>{{ article?.title }}</h1>
            <p class="time">
              最后更新：{{
                formatDateTime(article?.updatedAt || article?.updated_at)
              }}
            </p>
            <p class="meta-info">
              <span>👁 {{ viewCount }}</span>
              <span>❤️ {{ likeCount }}</span>
            </p>
            <p
              class="publish-time"
              v-if="article?.publishedAt || article?.published_at"
            >
              发布时间：{{
                formatDateTime(article?.publishedAt || article?.published_at)
              }}
            </p>
            <LikeButton
              :liked="liked"
              :count="likeCount"
              :loading="liking"
              @toggle="handleLike"
            />
          </div>
        </header>

        <div class="article-layout">
          <aside class="toc-wrap">
            <ArticleToc :toc="toc" />
          </aside>
          <section class="content-wrap page-panel">
            <div class="rich-content" v-html="safeHtml" />
          </section>
        </div>
      </article>

      <CommentForm
        :reply-to="replyTarget"
        @submit="handleSubmitComment"
        @cancel="handleCancelReply"
      />
      <CommentList
        :comments="comments"
        :loading="loadingComments"
        :hasMore="hasMoreComments"
        @load-more="loadMoreComments"
        @reply="handleReply"
      />
    </template>
  </section>
</template>

<style scoped>
.detail-view {
  display: grid;
  gap: 12px;
}

.back-bar {
  display: flex;
  justify-content: flex-start;
  padding: 8px 0;
}

.back-btn {
  border: 1px solid rgba(136, 170, 225, 0.38);
  background: rgba(7, 15, 26, 0.85);
  color: #c8d8f1;
  transition: all 0.28s ease;
}

.back-btn:hover {
  border-color: rgba(212, 166, 59, 0.6);
  background: rgba(212, 166, 59, 0.12);
  color: #f2d489;
}

.article-main {
  display: grid;
  gap: 14px;
  border-color: var(--border-strong);
}

.article-main::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    140deg,
    transparent 0 25%,
    rgba(31, 210, 255, 0.08) 38%,
    transparent 58%
  );
  animation: detailSweep 8s linear infinite;
}

.hero-head {
  position: relative;
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(220px, 0.9fr) 1.1fr;
  align-items: stretch;
}

.hero-image {
  width: 100%;
  height: 100%;
  min-height: 220px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid rgba(212, 166, 59, 0.42);
  box-shadow:
    0 0 0 1px rgba(31, 210, 255, 0.24),
    0 0 20px rgba(31, 210, 255, 0.18);
  filter: saturate(1.22) contrast(1.12);
}

.hero-info {
  border: 1px solid rgba(136, 170, 225, 0.38);
  border-radius: 12px;
  padding: 14px;
  background:
    linear-gradient(160deg, rgba(7, 15, 26, 0.85), rgba(10, 19, 34, 0.78)),
    url("/images/imperial-eagle.webp") right bottom / 200px auto no-repeat;
  box-shadow: inset 0 0 18px rgba(31, 210, 255, 0.08);
}

.signal {
  margin: 0;
  color: var(--accent-soft);
  letter-spacing: 0.14em;
  font-size: 12px;
}

.hero-info h1 {
  margin: 8px 0;
  text-shadow:
    0 0 10px rgba(31, 210, 255, 0.25),
    0 0 22px rgba(31, 210, 255, 0.15);
}

.time {
  margin: 0 0 12px;
  color: var(--text-muted);
}

.meta-info {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 14px;
}

.publish-time {
  margin: 0 0 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.article-layout {
  display: grid;
  gap: 12px;
  grid-template-columns: 260px minmax(0, 1fr);
}

.content-wrap {
  border-color: rgba(136, 170, 225, 0.34);
  background:
    linear-gradient(150deg, rgba(7, 14, 27, 0.95), rgba(8, 17, 30, 0.88)),
    repeating-linear-gradient(
      0deg,
      rgba(255, 255, 255, 0.012) 0,
      rgba(255, 255, 255, 0.012) 2px,
      transparent 2px,
      transparent 4px
    );
}

.rich-content :deep(h2),
.rich-content :deep(h3),
.rich-content :deep(h4) {
  scroll-margin-top: 90px;
  color: #f0f5ff;
  margin-top: 1.3em;
  margin-bottom: 0.6em;
  border-left: 3px solid rgba(212, 166, 59, 0.7);
  padding-left: 10px;
}

.rich-content :deep(p) {
  color: #d5e2f7;
  line-height: 1.82;
}

.rich-content :deep(code) {
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(10, 19, 35, 0.92);
  border: 1px solid rgba(132, 168, 228, 0.35);
  color: #f0d592;
}

.rich-content :deep(pre) {
  background: rgba(6, 13, 24, 0.92);
  border: 1px solid rgba(132, 168, 228, 0.35);
  border-radius: 12px;
  padding: 12px;
  overflow-x: auto;
}

@keyframes detailSweep {
  0% {
    transform: translateX(-70%);
  }
  100% {
    transform: translateX(70%);
  }
}

@media (max-width: 1060px) {
  .hero-head,
  .article-layout {
    grid-template-columns: 1fr;
  }
}
</style>
