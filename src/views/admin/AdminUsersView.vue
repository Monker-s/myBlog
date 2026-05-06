<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getAdminUsers,
  updateAdminUserRole,
  updateAdminUserStatus,
} from "@/api/admin";
import service from "@/utils/service";
import { unwrapData } from "@/utils/service";
import { formatDateTime } from "@/utils/date";

const loading = ref(false);
const editDialogVisible = ref(false);
const editSaving = ref(false);

const filters = reactive({
  keyword: "",
  page: 1,
  pageSize: 12,
  total: 0
});

const userList = ref([]);

const editForm = reactive({
  id: null,
  username: "",
  email: "",
  password: "",
});

const resetEditForm = () => {
  editForm.id = null;
  editForm.username = "";
  editForm.email = "";
  editForm.password = "";
};

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await getAdminUsers({
      keyword: filters.keyword,
      page: filters.page,
      pageSize: filters.pageSize
    });

    const data = unwrapData(res);
    if (Array.isArray(data)) {
      userList.value = data;
      filters.total = data.length;
    } else {
      userList.value = data?.records || [];
      filters.total = Number(data?.total || 0);
    }
  } catch (err) {
    ElMessage.error("获取用户列表失败");
    userList.value = [];
    filters.total = 0;
  } finally {
    loading.value = false;
  }
};

const openEditDialog = (row) => {
  resetEditForm();
  editForm.id = row.id;
  editForm.username = row.username || "";
  editForm.email = row.email || "";
  editForm.password = "";
  editDialogVisible.value = true;
};

const handleSaveEdit = async () => {
  if (!editForm.username.trim()) {
    ElMessage.warning("用户名不能为空");
    return;
  }

  if (!editForm.email.trim()) {
    ElMessage.warning("邮箱不能为空");
    return;
  }

  editSaving.value = true;
  try {
    const payload = {
      username: editForm.username,
      email: editForm.email,
    };

    if (editForm.password.trim()) {
      payload.password = editForm.password;
    }

    await service.put(`/api/admin/users/${editForm.id}`, payload);
    ElMessage.success("用户信息已更新");
    editDialogVisible.value = false;
    await fetchUsers();
  } catch (err) {
    ElMessage.error("更新失败，请稍后重试");
  } finally {
    editSaving.value = false;
  }
};

const toggleStatus = async (row) => {
  if (Number(row.role) === 99) {
    ElMessage.warning("Admin 账号不允许禁用");
    return;
  }

  const currentStatus = Number(row.status);
  const nextStatus = currentStatus === 1 ? 0 : 1;
  const actionText = nextStatus === 0 ? "禁用" : "启用";

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}用户「${row.username}」吗？`,
      `确认${actionText}`,
      {
        confirmButtonText: actionText,
        cancelButtonText: "取消",
        type: nextStatus === 0 ? "warning" : "info",
      },
    );

    await updateAdminUserStatus(row.id, nextStatus);
    row.status = nextStatus;
    ElMessage.success(`用户已${actionText}`);
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(`${actionText}失败，请稍后重试`);
    }
  }
};

onMounted(fetchUsers);
</script>

<template>
  <section class="admin-page">
    <header class="page-head">
      <h2>用户管理</h2>
      <el-form inline>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="用户名或邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
        </el-form-item>
      </el-form>
    </header>

    <el-table :data="userList" v-loading="loading" stripe class="admin-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="Number(row.role) === 99 ? 'danger' : 'success'">
            {{ Number(row.role) === 99 ? "Admin" : "User" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'">
            {{ Number(row.status) === 1 ? "正常" : "禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.last_login_at || row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="Number(row.role) !== 99"
              link
              :type="Number(row.status) === 1 ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ Number(row.status) === 1 ? "禁用" : "启用" }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="filters.page"
      v-model:page-size="filters.pageSize"
      background
      layout="prev, pager, next"
      :total="filters.total"
      class="pager"
      @current-change="fetchUsers"
    />

    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="480px"
      destroy-on-close
      class="edit-dialog"
    >
      <el-form :model="editForm" label-position="top" class="edit-form">
        <el-form-item label="用户名" required>
          <el-input v-model="editForm.username" maxlength="50" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="editForm.email" maxlength="100" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="editForm.password"
            type="password"
            maxlength="255"
            show-password
            placeholder="留空则不修改密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveEdit">保存</el-button>
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
  gap: 10px;
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

.pager {
  justify-content: flex-end;
}

.pager :deep(.el-pagination__total) {
  color: #4b5563;
}

.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.edit-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f9fafb, #f3f4f6);
  border-bottom: 2px solid #e5e7eb;
  padding: 16px 20px;
}

.edit-dialog :deep(.el-dialog__title) {
  color: #1f2937;
  font-weight: 600;
}

.edit-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

.edit-form :deep(.el-input__wrapper) {
  background: #ffffff;
  border-color: #d1d5db;
}

.edit-form :deep(.el-input__inner) {
  color: #1f2937;
}
</style>