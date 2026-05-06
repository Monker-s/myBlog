<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import service from "@/utils/service";
import { unwrapData } from "@/utils/service";

const loading = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const editingId = ref(null);

const categoryList = ref([]);

// 扁平化分类列表（用于父分类选择）
const flatCategories = computed(() => {
  const result = [];
  const flatten = (list, prefix = "") => {
    for (const item of list) {
      result.push({
        id: item.id,
        name: prefix + item.name,
      });
      if (item.children && item.children.length > 0) {
        flatten(item.children, prefix + "  ");
      }
    }
  };
  flatten(categoryList.value);
  return result;
});

const form = reactive({
  name: "",
  slug: "",
  parent_id: null,
  sort_order: 0,
  description: "",
});

const resetForm = () => {
  form.name = "";
  form.slug = "";
  form.parent_id = null;
  form.sort_order = 0;
  form.description = "";
};

const fetchCategories = async () => {
  loading.value = true;
  try {
    const res = await service.get("/api/admin/categories");
    const data = unwrapData(res);
    categoryList.value = Array.isArray(data) ? data : data?.records || data?.list || [];
  } catch (err) {
    ElMessage.error("获取分类列表失败");
    categoryList.value = [];
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
  form.parent_id = row.parent_id || null;
  form.sort_order = row.sort_order || 0;
  form.description = row.description || "";
  dialogVisible.value = true;
};

const saveCategory = async () => {
  if (!form.name.trim()) {
    ElMessage.warning("分类名称不能为空");
    return;
  }

  saving.value = true;
  try {
    const payload = {
      name: form.name,
      parent_id: form.parent_id || undefined,
      sort_order: form.sort_order || 0,
      description: form.description || undefined,
    };

    if (editingId.value) {
      await service.put(`/api/admin/categories/${editingId.value}`, payload);
      ElMessage.success("分类已更新");
    } else {
      await service.post("/api/admin/categories", payload);
      ElMessage.success("分类已创建");
    }

    dialogVisible.value = false;
    await fetchCategories();
  } catch {
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${row.name}」吗？`,
      "确认删除",
      {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await service.delete(`/api/admin/categories/${row.id}`);
    ElMessage.success("分类已删除");
    await fetchCategories();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败，请稍后重试");
    }
  }
};

onMounted(fetchCategories);
</script>

<template>
  <section class="admin-page">
    <header class="page-head">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建分类
      </el-button>
    </header>

    <el-table
      :data="categoryList"
      v-loading="loading"
      stripe
      class="admin-table"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="分类名称" min-width="150">
        <template #default="{ row }">
          <span class="name-text" :style="{ paddingLeft: row.parent_id ? '20px' : '0' }">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="slug" label="Slug" min-width="120">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.slug }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="text-muted">{{ row.description || "-" }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="post_count" label="文章数" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="success">{{ row.post_count || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新建分类'"
      width="500px"
      destroy-on-close
      class="category-dialog"
    >
      <el-form label-position="top" class="category-form">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parent_id" clearable placeholder="无（作为一级分类）" style="width: 100%">
            <el-option
              v-for="cat in flatCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
              :disabled="cat.id === editingId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="分类描述" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
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

.name-text {
  font-weight: 600;
  color: #1f2937;
}

.text-muted {
  color: #6b7280;
  font-size: 13px;
}

.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.category-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f9fafb, #f3f4f6);
  border-bottom: 2px solid #e5e7eb;
  padding: 16px 20px;
}

.category-dialog :deep(.el-dialog__title) {
  color: #1f2937;
  font-weight: 600;
}

.category-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

.category-form :deep(.el-input__wrapper) {
  background: #ffffff;
  border-color: #d1d5db;
}

.category-form :deep(.el-input__inner) {
  color: #1f2937;
}

.category-form :deep(.el-textarea__inner) {
  background: #ffffff;
  border-color: #d1d5db;
  color: #1f2937;
}
</style>