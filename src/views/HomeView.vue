<script setup>
import { ref, computed, onMounted } from "vue";
import ArticleCard from "@/components/articles/ArticleCard.vue";
import { useAppStore } from "@/stores/app";
import { setPageMeta } from "@/utils/seo";
import { getPosts } from "@/api/posts";
import { unwrapData } from "@/utils/service";

const appStore = useAppStore();

setPageMeta({
  title: "舰桥首页 | 个人知识库",
  description: "以战锤风格打造的个人知识中枢首页。",
});

// 首页置顶文章数据
const featuredPosts = ref([]);

// 加载置顶文章
const loadPinnedPosts = async () => {
  try {
    const res = await getPosts({
      pinned: "1",
      page: 1,
      pageSize: 10,
    });
    const data = unwrapData(res);
    featuredPosts.value = Array.isArray(data)
      ? data
      : data?.list || data?.records || [];
  } catch (error) {
    console.error("❌ 加载置顶文章失败:", error);
    featuredPosts.value = [];
  }
};

onMounted(() => {
  loadPinnedPosts();
});

const commandLogs = computed(() => [
  {
    id: 1,
    title: "第十章补充条款已编入主文档",
    time: "2026-04-08 14:08",
    status: "已归档",
  },
  {
    id: 2,
    title: "前端 API 契约层完成首轮联调",
    time: "2026-04-08 16:37",
    status: "进行中",
  },
  {
    id: 3,
    title: "详情页评论与点赞回滚逻辑通过",
    time: "2026-04-08 17:22",
    status: "已验证",
  },
]);
</script>

<template>
  <section class="home-view">
    <!-- 主英雄区：用你给的装甲图构建“战术中枢”第一印象 -->
    <article class="hero-panel page-panel">
      <div class="hero-copy">
        <p class="eyebrow">ULTRAMARINES DATA FORGE</p>
        <h1>{{ appStore.decoration.siteName || "个人知识库" }}</h1>
        <p class="desc">
          {{
            appStore.decoration.slogan ||
            "以军团纪律管理知识，以工程方法记录世界。"
          }}
        </p>
        <div class="hero-actions">
          <router-link class="action-btn primary" to="/articles"
            >进入文献库</router-link
          >
          <router-link class="action-btn" to="/notifications"
            >查看战报</router-link
          >
        </div>
      </div>
      <div class="hero-art" aria-hidden="true">
        <img src="/images/angel-ascend.webp" alt="" />
      </div>
      <span class="hero-ornament left" aria-hidden="true"></span>
      <span class="hero-ornament right" aria-hidden="true"></span>
    </article>

    <section class="section-head">
      <h2>精选战术文献</h2>
      <small>共 {{ featuredPosts.length }} 篇优先推荐</small>
    </section>

    <div class="post-grid">
      <ArticleCard
        v-for="item in featuredPosts"
        :key="item.id"
        :article="item"
      />
    </div>

    <section class="ops-board page-panel">
      <div class="ops-top">
        <h2>舰桥更新记录</h2>
        <span>实时同步</span>
      </div>

      <article v-for="log in commandLogs" :key="log.id" class="log-item">
        <div>
          <h3>{{ log.title }}</h3>
          <small>{{ log.time }}</small>
        </div>
        <el-tag :type="log.status === '进行中' ? 'warning' : 'success'">{{
          log.status
        }}</el-tag>
      </article>
    </section>
  </section>
</template>

<style scoped>
/* ===== 基础布局 ===== */
.home-view {
  display: grid;
  gap: 18px;
  background: repeating-linear-gradient(
    0deg,
    rgba(20, 15, 10, 0.03) 0px,
    rgba(20, 15, 10, 0.03) 1px,
    transparent 1px,
    transparent 3px
  );
  padding-bottom: 20px;
}

/* ===== 主英雄区：哥特式帝国圣殿风格 ===== */
.hero-panel {
  position: relative;
  display: grid;
  grid-template-columns: 1.2fr minmax(260px, 0.8fr);
  gap: 18px;
  border: 3px solid #8b7355;
  border-radius: 4px;
  overflow: hidden;
  background:
    linear-gradient(160deg, rgba(15, 10, 8, 0.97), rgba(25, 18, 12, 0.94)),
    linear-gradient(90deg, rgba(139, 115, 85, 0.15), transparent 60%);
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px #3d2e1c,
    0 0 0 6px #1a120b,
    0 8px 32px rgba(0, 0, 0, 0.8),
    inset 0 0 60px rgba(139, 115, 85, 0.08);
}

.hero-panel::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 28px,
      rgba(139, 115, 85, 0.04) 28px,
      rgba(139, 115, 85, 0.04) 29px
    ),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 28px,
      rgba(139, 115, 85, 0.04) 28px,
      rgba(139, 115, 85, 0.04) 29px
    );
}

/* 金色装饰边框 */
.hero-panel::after {
  content: "";
  position: absolute;
  inset: 8px;
  border: 2px solid rgba(184, 148, 95, 0.35);
  border-radius: 2px;
  pointer-events: none;
}

.hero-copy {
  position: relative;
  z-index: 1;
  align-self: center;
  padding: 24px 28px;
}

.eyebrow {
  margin: 0;
  color: #c4a265;
  letter-spacing: 0.22em;
  font-size: 13px;
  text-transform: uppercase;
  font-weight: 600;
  text-shadow: 0 0 12px rgba(196, 162, 101, 0.3);
  border-bottom: 2px solid rgba(184, 148, 95, 0.4);
  padding-bottom: 8px;
  display: inline-block;
}

.hero-panel h1 {
  margin: 14px 0 10px;
  font-size: clamp(2rem, 4vw, 3.2rem);
  color: #e8dcc8;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 0 20px rgba(196, 162, 101, 0.25);
  position: relative;
}

/* 标题下方金色装饰线 */
.hero-panel h1::after {
  content: "◆";
  position: absolute;
  bottom: -14px;
  left: 0;
  color: #b8945f;
  font-size: 14px;
  text-shadow: 0 0 8px rgba(184, 148, 95, 0.5);
}

.desc {
  margin: 18px 0 0;
  color: #9a8b78;
  max-width: 55ch;
  line-height: 1.7;
  font-size: 15px;
  letter-spacing: 0.02em;
}

.hero-actions {
  margin-top: 20px;
  display: flex;
  gap: 14px;
}

.action-btn {
  text-decoration: none;
  border: 2px solid #7a6650;
  border-radius: 3px;
  color: #d4c4a8;
  padding: 10px 20px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.25s ease;
  background: linear-gradient(
    180deg,
    rgba(60, 45, 30, 0.6),
    rgba(35, 25, 18, 0.8)
  );
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(184, 148, 95, 0.15);
  position: relative;
}

.action-btn::before {
  content: "";
  position: absolute;
  inset: 3px;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 2px;
  pointer-events: none;
}

.action-btn:hover {
  transform: translateY(-2px);
  border-color: #b8945f;
  color: #f0e6d2;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(184, 148, 95, 0.2),
    inset 0 1px 0 rgba(184, 148, 95, 0.3);
}

.action-btn.primary {
  border-color: #b8945f;
  color: #f5e6c8;
  background: linear-gradient(
    180deg,
    rgba(139, 115, 85, 0.35),
    rgba(75, 55, 35, 0.6)
  );
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.6),
    0 0 16px rgba(184, 148, 95, 0.15),
    inset 0 1px 0 rgba(212, 180, 120, 0.25);
}

.action-btn.primary:hover {
  border-color: #d4a63b;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.7),
    0 0 28px rgba(212, 166, 59, 0.3),
    inset 0 1px 0 rgba(212, 166, 59, 0.35);
}

.hero-art {
  position: relative;
  z-index: 1;
  align-self: stretch;
  border-radius: 2px;
  overflow: hidden;
  border: 2px solid #6b5642;
  margin: 12px;
  margin-left: 0;
}

.hero-art::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(20, 15, 10, 0.2),
    rgba(20, 15, 10, 0.85)
  );
  z-index: 1;
}

.hero-art::after {
  content: "";
  position: absolute;
  inset: 4px;
  border: 1px solid rgba(139, 115, 85, 0.25);
  pointer-events: none;
  z-index: 2;
}

.hero-art img {
  width: 100%;
  height: 100%;
  min-height: 280px;
  object-fit: cover;
  filter: saturate(0.85) contrast(1.18) brightness(0.9) sepia(0.15);
}

/* 四角金色铆钉装饰 */
.hero-ornament {
  position: absolute;
  width: 32px;
  height: 32px;
  pointer-events: none;
  z-index: 3;
}

.hero-ornament::before,
.hero-ornament::after {
  content: "";
  position: absolute;
  background: #b8945f;
  box-shadow: 0 0 8px rgba(184, 148, 95, 0.5);
}

.hero-ornament::before {
  width: 100%;
  height: 3px;
}

.hero-ornament::after {
  width: 3px;
  height: 100%;
}

.hero-ornament.left {
  left: 14px;
  top: 14px;
}

.hero-ornament.right {
  right: 14px;
  bottom: 14px;
}

/* 角落铆钉圆点 */
.hero-ornament.left::before,
.hero-ornament.left::after,
.hero-ornament.right::before,
.hero-ornament.right::after {
  border-radius: 50%;
  background: radial-gradient(circle, #d4a63b, #8b7355);
}

.hero-ornament.left::before {
  width: 8px;
  height: 8px;
  top: 0;
  left: 0;
}

.hero-ornament.left::after {
  width: 8px;
  height: 8px;
  bottom: 0;
  left: 0;
}

.hero-ornament.right::before {
  width: 8px;
  height: 8px;
  top: 0;
  right: 0;
}

.hero-ornament.right::after {
  width: 8px;
  height: 8px;
  bottom: 0;
  right: 0;
}

/* ===== 区块标题 ===== */
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  padding: 12px 0 6px;
  border-bottom: 3px solid #3d2e1c;
  position: relative;
}

.section-head::after {
  content: "";
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, #b8945f, transparent);
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  color: #d4c4a8;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
}

.section-head small {
  color: #8b7d6b;
  font-size: 13px;
}

/* ===== 文章网格 ===== */
.post-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

/* ===== 舰桥更新记录面板 ===== */
.ops-board {
  border: 3px solid #6b5642;
  border-radius: 4px;
  background:
    linear-gradient(160deg, rgba(20, 15, 10, 0.96), rgba(30, 22, 15, 0.92)),
    repeating-linear-gradient(
      -45deg,
      rgba(60, 45, 30, 0.08) 0px,
      rgba(60, 45, 30, 0.08) 2px,
      transparent 2px,
      transparent 4px
    );
  padding: 20px;
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 4px #3d2e1c,
    0 6px 24px rgba(0, 0, 0, 0.7),
    inset 0 0 40px rgba(100, 80, 55, 0.06);
  position: relative;
}

.ops-board::before {
  content: "";
  position: absolute;
  inset: 6px;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 2px;
  pointer-events: none;
}

.ops-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 2px solid #3d2e1c;
  margin-bottom: 16px;
}

.ops-top::after {
  content: "— ◆ —";
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  color: #b8945f;
  font-size: 10px;
  letter-spacing: 0.3em;
  background: rgba(25, 18, 12, 0.95);
  padding: 0 12px;
}

.ops-top h2 {
  margin: 0;
  font-size: 18px;
  color: #d4c4a8;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-weight: 700;
}

.ops-top span {
  color: #a89070;
  font-size: 13px;
  letter-spacing: 0.05em;
}

.log-item {
  margin-top: 14px;
  padding: 12px 16px 12px 28px;
  border-top: 1px solid rgba(100, 80, 55, 0.25);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  position: relative;
  background: linear-gradient(90deg, rgba(60, 45, 30, 0.12), transparent 40%);
  transition: background 0.2s ease;
}

.log-item:hover {
  background: linear-gradient(90deg, rgba(80, 60, 40, 0.2), transparent 50%);
}

.log-item::before {
  content: "";
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  background: radial-gradient(circle, #d4a63b, #8b6914);
  box-shadow:
    0 0 8px rgba(212, 166, 59, 0.5),
    inset 0 1px 2px rgba(255, 255, 255, 0.3);
  clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
}

.log-item h3 {
  margin: 0;
  font-size: 15px;
  color: #c8b89a;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.log-item small {
  color: #7a6b58;
  font-size: 12px;
  display: block;
  margin-top: 4px;
}

/* ===== 响应式 ===== */
@media (max-width: 1060px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-art {
    margin: 12px;
    min-height: 200px;
  }

  .post-grid {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .hero-panel h1 {
    font-size: 1.6rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .action-btn {
    text-align: center;
  }
}
</style>
