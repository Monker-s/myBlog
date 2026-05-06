<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { ROUTE_NAMES } from "@/constants/routes";
import {
  deleteAdminPost,
  getAdminPosts,
} from "@/api/admin";
import { unwrapData } from "@/utils/service";
import { formatDateTime } from "@/utils/date";
import service from "@/utils/service";

const router = useRouter();
const loading = ref(false);
const deleting = ref(false);

const categoryOptions = ref([]);

const fetchCategories = async () => {
  try {
    const res = await service.get("/api/admin/categories");
    const data = unwrapData(res);
    categoryOptions.value = Array.isArray(data) ? data : data?.records || data?.list || [];
  } catch {
    ElMessage.error("获取分类列表失败");
    categoryOptions.value = [];
  }
};

const filters = reactive({
  keyword: "",
  status: "",
  category: "",
  sort: "updated_at:desc",
  page: 1,
  pageSize: 20,
  total: 0,
});

const postList = ref([]);

const fetchPosts = async () => {
  loading.value = true;
  try {
    const res = await getAdminPosts({
      keyword: filters.keyword,
      status: filters.status !== "" ? filters.status : undefined,
      category: filters.category || undefined,
      sort: filters.sort || "updated_at:desc",
      page: filters.page,
      pageSize: filters.pageSize,
    });

    const data = unwrapData(res);
    if (Array.isArray(data)) {
      postList.value = data;
      filters.total = data.length;
    } else {
      postList.value = data?.records || data?.items || [];
      filters.total = Number(data?.total || 0);
    }
  } catch (err) {
    ElMessage.error("获取文章列表失败");
    postList.value = [];
    filters.total = 0;
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  router.push({ name: ROUTE_NAMES.ADMIN_POST_CREATE });
};

const openEdit = (row) => {
  router.push({ name: ROUTE_NAMES.ADMIN_POST_EDIT, params: { id: row.id } });
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章「${row.title}」吗？`,
      "确认删除",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    deleting.value = true;
    await deleteAdminPost(row.id);
    ElMessage.success("文章已删除");
    await fetchPosts();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败，请稍后重试");
    }
  } finally {
    deleting.value = false;
  }
};

const handleReset = () => {
  filters.keyword = "";
  filters.status = "";
  filters.category = "";
  filters.sort = "updated_at:desc";
  filters.page = 1;
  fetchPosts();
};

onMounted(() => {
  fetchCategories();
  fetchPosts();
});
</script>

<template>
  <section class="page-panel admin-page">
    <header class="page-head">
      <h2>文章管理</h2>
      <el-button type="warning" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建文章
      </el-button>
    </header>

    <el-form inline class="filter-row">
      <el-form-item label="关键词">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="标题/摘要"
          style="width: 180px"
          @keyup.enter="fetchPosts"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="filters.status"
          clearable
          placeholder="全部"
          style="width: 130px"
        >
          <el-option label="全部" value="" />
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="隐藏" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select
          v-model="filters.category"
          clearable
          placeholder="全部分类"
          style="width: 150px"
        >
          <el-option
            v-for="cat in categoryOptions"
            :key="cat.id"
            :label="cat.name"
            :value="cat.slug"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-select v-model="filters.sort" style="width: 160px">
          <el-option label="更新时间降序" value="updated_at:desc" />
          <el-option label="更新时间升序" value="updated_at:asc" />
          <el-option label="创建时间降序" value="created_at:desc" />
          <el-option label="浏览量降序" value="view_count:desc" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchPosts">筛选</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="postList" v-loading="loading" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column
        prop="title"
        label="标题"
        min-width="240"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <div class="title-cell">
            <span class="title-text">{{ row.title }}</span>
            <el-tag
              v-if="Number(row.is_pinned) === 1"
              size="small"
              type="danger"
              class="pin-tag"
              >置顶</el-tag
            >
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="
              Number(row.status) === 1
                ? 'success'
                : Number(row.status) === 0
                  ? 'info'
                  : 'danger'
            "
            size="small"
          >
            {{
              Number(row.status) === 1
                ? "已发布"
                : Number(row.status) === 0
                  ? "草稿"
                  : "隐藏"
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="text-muted">{{ row.category?.name || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="tags-cell">
            <el-tag
              v-for="tag in (row.tags || []).slice(0, 2)"
              :key="tag.id || tag"
              size="small"
              type="primary"
              class="tag-item"
            >
              {{ tag.name || tag }}
            </el-tag>
            <span v-if="(row.tags || []).length > 2" class="text-muted"
              >+{{ (row.tags || []).length - 2 }}</span
            >
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="view_count"
        label="浏览"
        width="80"
        align="center"
        sortable
      />
      <el-table-column
        prop="like_count"
        label="点赞"
        width="80"
        align="center"
        sortable
      />
      <el-table-column
        prop="comment_count"
        label="评论"
        width="80"
        align="center"
        sortable
      />
      <el-table-column label="更新时间" width="170" align="center">
        <template #default="{ row }">
          <span class="text-time">{{ formatDateTime(row.updated_at) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button link type="primary" @click="openEdit(row)"
              >编辑</el-button
            >
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              :loading="deleting"
              >删除</el-button
            >
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="filters.page"
      v-model:page-size="filters.pageSize"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="filters.total"
      :page-sizes="[10, 20, 50, 100]"
      class="pager"
      @current-change="fetchPosts"
      @size-change="fetchPosts"
    />
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
  padding: 24px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f3f4f6;
}

.page-head h2 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 22px;
  color: #1f2937;
}

.filter-row {
  margin-bottom: -8px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
}

.filter-row :deep(.el-form-item) {
  margin-bottom: 8px;
}

.filter-row :deep(.el-form-item__label) {
  color: #4b5563;
  font-size: 13px;
}

.admin-table {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.admin-table :deep(.el-table__header) {
  background: #f3f4f6;
}

.admin-table :deep(.el-table__header th) {
  color: #374151;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.admin-table :deep(.el-table__row) {
  background: #ffffff;
  transition: background 0.2s ease;
}

.admin-table :deep(.el-table__row:hover) {
  background: #f0f4ff;
}

.admin-table :deep(.el-table__cell) {
  border-color: #e5e7eb;
  color: #1f2937;
}

.admin-table :deep(.el-table__row--striped) {
  background: #f9fafb;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: #1f2937;
}

.pin-tag {
  flex-shrink: 0;
}

.tags-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 2px;
}

.text-muted {
  color: #6b7280;
  font-size: 12px;
}

.text-time {
  color: #6b7280;
  font-size: 12px;
}

.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.pager :deep(.el-pagination__total) {
  color: #4b5563;
}

.pager :deep(.el-pagination__jump) {
  color: #4b5563;
}

@media (max-width: 1200px) {
  .filter-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .admin-page {
    padding: 12px;
    gap: 12px;
  }

  .page-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .pager {
    justify-content: center;
  }
}
</style>
