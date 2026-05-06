<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useNotificationStore } from "@/stores/notifications";
import { formatDateTime } from "@/utils/date";
import { setPageMeta } from "@/utils/seo";

setPageMeta({
  title: "通知中心 | 个人知识库",
  description: "查看文章更新提醒并管理已读状态。"
});

const notificationStore = useNotificationStore();
const onlyUnread = ref(false);

const renderList = computed(() => {
  if (!onlyUnread.value) return notificationStore.list;
  return notificationStore.list.filter((item) => Number(item.is_read) !== 1);
});

const loadNotifications = async () => {
  try {
    await Promise.all([notificationStore.fetchList({ page: 1, pageSize: 50 }), notificationStore.fetchUnread()]);
  } catch {
    ElMessage.error("通知加载失败");
  }
};

const markOne = async (id) => {
  try {
    await notificationStore.markRead([id]);
    ElMessage.success("已标记为已读");
  } catch {
    ElMessage.error("操作失败");
  }
};

const markAll = async () => {
  try {
    await notificationStore.markAllRead();
    ElMessage.success("全部标记为已读");
  } catch {
    ElMessage.error("操作失败");
  }
};

onMounted(loadNotifications);
</script>

<template>
  <section class="page-panel notify-view">
    <header class="notify-head">
      <h1>通知中心</h1>
      <div class="actions">
        <el-switch v-model="onlyUnread" active-text="仅看未读" />
        <el-button @click="markAll">全部已读</el-button>
      </div>
    </header>

    <el-empty v-if="!renderList.length" description="暂无通知" />

    <article v-for="item in renderList" :key="item.id" class="notify-item">
      <header>
        <strong>{{ item.title || "文章更新提醒" }}</strong>
        <small>{{ formatDateTime(item.created_at || item.createdAt) }}</small>
      </header>
      <p>{{ item.body || "你关注的文章有更新，点击查看详情。" }}</p>
      <footer>
        <el-tag :type="Number(item.is_read) === 1 ? 'info' : 'warning'">
          {{ Number(item.is_read) === 1 ? "已读" : "未读" }}
        </el-tag>
        <el-button v-if="Number(item.is_read) !== 1" size="small" @click="markOne(item.id)">
          标记已读
        </el-button>
      </footer>
    </article>
  </section>
</template>

<style scoped>
.notify-view {
  display: grid;
  gap: 10px;
}

.notify-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.notify-head h1 {
  margin: 0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notify-item {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 10px;
}

.notify-item header,
.notify-item footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.notify-item p {
  margin: 8px 0;
  color: var(--text-muted);
}
</style>