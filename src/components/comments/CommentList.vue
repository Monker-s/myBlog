<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { formatDateTime } from "@/utils/date";

const props = defineProps({
  comments: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  hasMore: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["loadMore", "reply"]);

const commentListRef = ref(null);
const loadingIndicatorRef = ref(null);

// 兼容后端可能的不同字段名
const getReplies = (comment) => {
  return (
    comment.replies ||
    comment.repliesData ||
    comment.replyList ||
    comment.children ||
    []
  );
};

// 触发回复
const handleReply = (comment) => {
  emit("reply", comment);
};

// 使用 IntersectionObserver 监听滚动
let observer = null;

const setupObserver = async () => {
  if (!loadingIndicatorRef.value) return;

  if (observer) {
    observer.disconnect();
  }

  observer = new IntersectionObserver(
    (entries) => {
      const target = entries[0];
      console.log(
        "[CommentList] IntersectionObserver:",
        target.isIntersecting,
        "hasMore:",
        props.hasMore,
        "loading:",
        props.loading,
      );
      // 当加载提示进入视口且还有更多数据、未处于加载状态时触发
      if (target.isIntersecting && props.hasMore && !props.loading) {
        console.log("[CommentList] 触发加载更多");
        emit("loadMore");
      }
    },
    {
      root: null, // 相对于视口
      rootMargin: "200px", // 提前 200px 加载
      threshold: 0.1,
    },
  );

  await nextTick();
  if (loadingIndicatorRef.value) {
    observer.observe(loadingIndicatorRef.value);
    console.log("[CommentList] 开始监听加载更多指示器");
  }
};

onMounted(() => {
  setupObserver();
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});

// 监听评论数据变化，重新设置观察器
watch(
  () => props.comments.length,
  async () => {
    await nextTick();
    setupObserver();
  },
);
</script>

<template>
  <section
    class="comment-list page-panel"
    aria-label="评论列表"
    ref="commentListRef"
  >
    <h3>评论（{{ props.comments.length }}）</h3>

    <el-empty
      v-if="!props.comments.length && !props.loading"
      description="暂无评论"
    />

    <article v-for="item in props.comments" :key="item.id" class="comment-item">
      <header class="comment-header">
        <div class="user-info">
          <strong>{{ item.userName || item.username || "匿名用户" }}</strong>
          <small>{{ formatDateTime(item.created_at || item.createdAt) }}</small>
        </div>
        <el-button
          class="reply-btn"
          size="small"
          text
          @click="handleReply(item)"
        >
          回复
        </el-button>
      </header>
      <p class="comment-content">{{ item.content }}</p>

      <!-- 文档定义为两级评论，这里只渲染一层 replies -->
      <!-- 兼容多种字段名：replies、repliesData、replyList、children -->
      <div v-if="getReplies(item)?.length" class="replies">
        <article
          v-for="reply in getReplies(item)"
          :key="reply.id"
          class="reply-item"
        >
          <header class="reply-header">
            <div class="user-info">
              <strong>{{
                reply.username || reply.userName || "匿名用户"
              }}</strong>
              <small>{{
                formatDateTime(
                  reply.created_at || reply.createdAt || reply.createdTime,
                )
              }}</small>
            </div>
            <el-button
              class="reply-btn"
              size="small"
              text
              @click="handleReply(reply)"
            >
              回复
            </el-button>
          </header>
          <p class="comment-content">{{ reply.content }}</p>
        </article>
      </div>
    </article>

    <!-- 加载更多提示 -->
    <div v-if="props.hasMore" ref="loadingIndicatorRef" class="loading-more">
      <el-skeleton v-if="props.loading" :rows="2" animated />
      <p v-else class="load-more-hint">上拉加载更多</p>
    </div>

    <!-- 没有更多提示 -->
    <div v-else-if="props.comments.length > 0" class="no-more">
      <p>没有更多评论了</p>
    </div>
  </section>
</template>

<style scoped>
.comment-item {
  border-top: 1px dashed var(--border-color);
  padding-top: 10px;
  margin-top: 10px;
}

.comment-item:first-of-type {
  border-top: 0;
  margin-top: 0;
  padding-top: 0;
}

.comment-header,
.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-header small,
.reply-header small {
  color: var(--text-muted);
}

.reply-btn {
  color: #88aae1;
  padding: 2px 6px;
  font-size: 12px;
  flex-shrink: 0;
}

.reply-btn:hover {
  color: #f2d489;
  background: rgba(212, 166, 59, 0.12);
}

.comment-content {
  margin: 8px 0;
  line-height: 1.6;
}

.replies {
  margin-top: 8px;
  padding-left: 14px;
  border-left: 2px solid var(--border-color);
  display: grid;
  gap: 8px;
}

.reply-item {
  background: rgba(10, 19, 35, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 8px;
  color: #d5e2f7;
}

.loading-more {
  margin-top: 16px;
  padding: 12px 0;
}

.load-more-hint {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  padding: 8px;
  border: 1px dashed var(--border-color);
  border-radius: 6px;
  background: rgba(136, 170, 225, 0.05);
  transition: all 0.2s ease;
}

.load-more-hint:hover {
  color: var(--accent-color);
  border-color: var(--accent-color);
  background: rgba(31, 210, 255, 0.08);
}

.no-more {
  margin-top: 16px;
  padding: 12px 0;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px dashed var(--border-color);
}
</style>
