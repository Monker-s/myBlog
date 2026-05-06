<script setup>
import { onMounted, reactive, ref } from "vue";
import ArticleFilterBar from "@/components/articles/ArticleFilterBar.vue";
import ArticleCard from "@/components/articles/ArticleCard.vue";
import { getPosts } from "@/api/posts";
import { unwrapData } from "@/utils/service";
import { usePagination } from "@/composables/usePagination";
import { setPageMeta } from "@/utils/seo";
import { ElMessage } from "element-plus";

setPageMeta({
  title: "圣典文献库 | 个人知识库",
  description: "以战术视图浏览所有文章。",
});

const categories = ref([
  { id: 1, name: "战术圣典" },
  { id: 2, name: "后勤与部署" },
  { id: 3, name: "系统架构" },
  { id: 4, name: "生活纪要" },
]);

const filters = reactive({
  keyword: "",
  category: "",
  sort: "latest",
});

const loading = ref(false);
const postList = ref([]);
const { page, pageSize, total } = usePagination(1, 10);

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getPosts({
      ...filters,
      page: page.value,
      pageSize: pageSize.value,
    });

    const data = unwrapData(res);

    if (Array.isArray(data)) {
      postList.value = data;
      total.value = data.length;
    } else {
      postList.value = data?.records || data?.list || [];
      total.value = Number(data?.total || postList.value.length);
    }
  } catch (error) {
    ElMessage.error(error?.message || "获取文章列表失败");
    postList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const onSearch = async () => {
  page.value = 1;
  await fetchList();
};

onMounted(fetchList);
</script>

<template>
  <section class="articles-view">
    <article class="deck-banner page-panel">
      <div>
        <p>ARCHIVE DECK</p>
        <h1>战术文献库</h1>
        <span>筛选关键字、分类与排序，快速定位目标文章。</span>
      </div>
    </article>

    <ArticleFilterBar
      v-model="filters"
      :categories="categories"
      @search="onSearch"
    />

    <section class="list-block page-panel">
      <header class="list-head">
        <h2>文献清单</h2>
        <small>共 {{ total }} 篇</small>
      </header>

      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="!postList.length" description="暂无符合条件的文章" />

      <div v-else class="list-grid">
        <ArticleCard v-for="item in postList" :key="item.id" :article="item" />
      </div>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        class="pager"
        background
        layout="prev, pager, next"
        :total="total"
        @current-change="fetchList"
      />
    </section>
  </section>
</template>

<style scoped>
.articles-view {
  display: grid;
  gap: 12px;
}

.deck-banner {
  border-color: var(--border-strong);
  background:
    linear-gradient(
      110deg,
      rgba(5, 14, 28, 0.95),
      rgba(8, 17, 31, 0.68) 45%,
      rgba(8, 17, 31, 0.28) 100%
    ),
    url("/images/ultramarines-squad.png") center/cover no-repeat;
}

.deck-banner p {
  margin: 0;
  color: var(--accent-soft);
  letter-spacing: 0.14em;
}

.deck-banner h1 {
  margin: 7px 0;
}

.deck-banner span {
  color: var(--text-muted);
}

.list-block {
  border-color: rgba(138, 169, 219, 0.42);
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.list-head h2 {
  margin: 0;
}

.list-head small {
  color: var(--text-muted);
}

.list-grid {
  margin-top: 10px;
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

@media (max-width: 1060px) {
  .list-grid {
    grid-template-columns: 1fr;
  }
}
</style>
