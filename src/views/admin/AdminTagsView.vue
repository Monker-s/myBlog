<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import service from "@/utils/service";
import { unwrapData } from "@/utils/service";

const loading = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const editingId = ref(null);

const tagList = ref([]);

const form = reactive({
  name: "",
  slug: "",
});

const resetForm = () => {
  form.name = "";
  form.slug = "";
};

const fetchTags = async () => {
  loading.value = true;
  try {
    const res = await service.get("/api/admin/tags");
    const data = unwrapData(res);
    tagList.value = Array.isArray(data) ? data : data?.list || [];
  } catch (err) {
    ElMessage.error("获取标签列表失败");
    tagList.value = [];
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row) => {
  editingId.value = row.id;
  form.name = row.name || "";
  form.slug = row.slug || "";
  dialogVisible.value = true;
};

const saveTag = async () => {
  if (!form.name.trim()) {
    ElMessage.warning("标签名称不能为空");
    return;
  }

  saving.value = true;
  try {
    const payload = {
      name: form.name,
      slug: form.slug || undefined,
    };

    if (editingId.value) {
      await service.put(`/api/admin/tags/${editingId.value}`, payload);
      ElMessage.success("标签已更新");
    } else {
      await service.post("/api/admin/tags", payload);
      ElMessage.success("标签已创建");
    }

    dialogVisible.value = false;
    await fetchTags();
  } catch {
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除标签「${row.name}」吗？`,
      "确认删除",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await service.delete(`/api/admin/tags/${row.id}`);
    ElMessage.success("标签已删除");
    await fetchTags();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败，请稍后重试");
    }
  }
};

onMounted(fetchTags);
</script>

<template>
  <section class="admin-page">
    <header class="page-head">
      <h2>标签管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建标签
      </el-button>
    </header>

    <div class="tags-grid">
      <div
        v-for="tag in tagList"
        :key="tag.id"
        class="tag-card"
      >
        <div class="tag-card-header">
          <span class="tag-name">{{ tag.name }}</span>
          <el-tag size="small" type="info">{{ tag.slug }}</el-tag>
        </div>
        <div class="tag-card-body">
          <span class="tag-count">{{ tag.post_count || 0 }} 篇文章</span>
        </div>
        <div class="tag-card-actions">
          <el-button link type="primary" size="small" @click="openEdit(tag)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(tag)">删除</el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && tagList.length === 0" description="暂无标签" />

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑标签' : '新建标签'"
      width="450px"
      destroy-on-close
      class="tag-dialog"
    >
      <el-form label-position="top" class="tag-form">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.name" maxlength="30" show-word-limit placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="Slug">
          <el-input v-model="form.slug" maxlength="30" placeholder="URL友好标识，如：vue" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveTag">保存</el-button>
      </template>
    </el-dialog>
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
  font-size: 20px;
  color: #1f2937;
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.tag-card {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s ease;
}

.tag-card:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
}

.tag-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tag-name {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
}

.tag-card-body {
  margin-bottom: 12px;
}

.tag-count {
  font-size: 13px;
  color: #6b7280;
}

.tag-card-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.tag-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f9fafb, #f3f4f6);
  border-bottom: 2px solid #e5e7eb;
  padding: 16px 20px;
}

.tag-dialog :deep(.el-dialog__title) {
  color: #1f2937;
  font-weight: 600;
}

.tag-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

.tag-form :deep(.el-input__wrapper) {
  background: #ffffff;
  border-color: #d1d5db;
}

.tag-form :deep(.el-input__inner) {
  color: #1f2937;
}
</style>
