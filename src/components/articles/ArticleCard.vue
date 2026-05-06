<script setup>
import { computed } from "vue";
import { formatDateTime } from "@/utils/date";
import { ROUTE_NAMES } from "@/constants/routes";

const props = defineProps({
  article: {
    type: Object,
    required: true,
  },
});

const postTags = computed(() => props.article.tags || []);
const cover = computed(
  () =>
    props.article.coverUrl ||
    props.article.cover_url ||
    "/images/ultra-helmet-alt.png",
);
</script>

<template>
  <article class="article-card page-panel">
    <div class="cover-wrap">
      <img :src="cover" alt="文章封面" class="cover" />
      <span class="pin" v-if="props.article.pinned || props.article.is_pinned"
        >置顶</span
      >
    </div>

    <header>
      <h3>
        <router-link
          :to="{
            name: ROUTE_NAMES.ARTICLE_DETAIL,
            params: { id: props.article.id },
          }"
        >
          {{ props.article.title }}
        </router-link>
      </h3>
      <p class="summary">{{ props.article.summary || "暂无摘要" }}</p>
    </header>

    <footer>
      <div class="meta">
        <span
          >更新
          {{
            formatDateTime(props.article.updatedAt || props.article.updated_at)
          }}</span
        >
        <span
          >✦
          {{ props.article.likeCount ?? props.article.like_count ?? 0 }}</span
        >
        <span
          >◉
          {{ props.article.viewCount ?? props.article.view_count ?? 0 }}</span
        >
      </div>
      <div class="tags">
        <span v-for="tag in postTags" :key="tag">{{ tag }}</span>
      </div>
    </footer>
  </article>
</template>

<style scoped>
.article-card {
  position: relative;
  display: grid;
  gap: 10px;
  border-color: rgba(132, 168, 228, 0.45);
  transition: 0.28s ease;
}

.article-card::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    120deg,
    transparent 0 36%,
    rgba(31, 210, 255, 0.14) 46%,
    transparent 58%
  );
  opacity: 0;
  transition: 0.24s ease;
}

.article-card:hover {
  transform: translateY(-3px);
  border-color: rgba(212, 166, 59, 0.6);
  box-shadow:
    0 16px 30px rgba(2, 8, 20, 0.52),
    inset 0 0 0 1px rgba(212, 166, 59, 0.24);
}

.article-card:hover::after {
  opacity: 1;
}

.cover-wrap {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(212, 166, 59, 0.45);
}

.cover-wrap::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: linear-gradient(
    180deg,
    rgba(5, 10, 18, 0.02),
    rgba(5, 10, 18, 0.74)
  );
}

.cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  filter: saturate(1.22) contrast(1.12);
  transform: scale(1.02);
  transition: 0.35s ease;
}

.article-card:hover .cover {
  transform: scale(1.07);
}

.pin {
  position: absolute;
  z-index: 2;
  top: 8px;
  right: 8px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  background: linear-gradient(
    145deg,
    rgba(6, 14, 25, 0.95),
    rgba(22, 33, 51, 0.86)
  );
  border-radius: 6px;
  color: #f0d592;
  font-size: 12px;
  letter-spacing: 0.08em;
  padding: 3px 9px;
}

.article-card h3 {
  margin: 0;
}

.article-card h3 a {
  color: #f2f6ff;
  text-decoration: none;
}

.article-card h3 a:hover {
  color: var(--primary-glow);
}

.summary {
  margin: 8px 0 0;
  color: var(--text-muted);
  line-height: 1.62;
}

.meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #a9bce0;
}

.tags {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tags span {
  border: 1px solid rgba(136, 170, 225, 0.4);
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
  color: #d2def4;
  background: rgba(8, 18, 32, 0.7);
}
</style>
