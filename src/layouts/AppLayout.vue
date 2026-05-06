<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import AppHeader from "@/components/common/AppHeader.vue";
import AppSidebar from "@/components/common/AppSidebar.vue";
import AppFooter from "@/components/common/AppFooter.vue";
import { useAuthStore } from "@/stores/auth";
import { useNotificationStore } from "@/stores/notifications";
import { useAppStore } from "@/stores/app";
import { ROUTE_NAMES } from "@/constants/routes";

const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const appStore = useAppStore();
const router = useRouter();

const drawerVisible = ref(false);

const stats = computed(() => [
  { label: "文献总数", value: 128 },
  { label: "圣印标签", value: 42 },
  { label: "今日访问", value: 357 },
  { label: "未读战报", value: notificationStore.unreadCount },
]);

/**
 * 全站统一退出：
 * 1) 清理会话态（真实后端 / mock 都兼容）
 * 2) 重置未读角标
 * 3) 跳回登录页
 */
const handleLogout = async () => {
  try {
    await authStore.logoutCurrentUser();
    notificationStore.unreadCount = 0;
    ElMessage.success("已退出登录");
    await router.replace({ name: ROUTE_NAMES.AUTH });
  } catch {
    ElMessage.error("退出失败，请稍后重试");
  }
};

onMounted(async () => {
  await Promise.allSettled([
    appStore.loadDecorationConfig(),
    authStore.restoreSession(),
    notificationStore.fetchUnread(),
  ]);
});
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳过导航直达正文</a>

    <AppHeader
      :site-name="appStore.decoration.siteName"
      :site-slogan="appStore.decoration.slogan"
      :user="authStore.user"
      :unread-count="notificationStore.unreadCount"
      @toggle-sidebar="drawerVisible = true"
      @logout="handleLogout"
    />

    <!-- 全站共享主视觉，确保每个页面都具备战锤风基调 -->
    <section class="command-banner">
      <div class="command-content">
        <h1 class="command-title">Macragge Command Deck</h1>
        <p class="command-sub">
          记录每一次部署、每一次修复与每一次胜利。让知识库像战术甲板一样可靠。
        </p>
        <span class="command-chip">Ultramarines Codex</span>
      </div>
    </section>

    <div class="app-grid">
      <aside class="app-sidebar">
        <AppSidebar :stats="stats" />
      </aside>

      <main id="main-content" class="app-main" aria-live="polite">
        <!-- 这里必须是 router-view，供子路由页面渲染 -->
        <router-view />
      </main>
    </div>

    <AppFooter :site-name="appStore.decoration.siteName" />

    <el-drawer
      v-model="drawerVisible"
      size="86%"
      direction="ltr"
      title="战术侧栏"
      :with-header="true"
    >
      <AppSidebar :stats="stats" />
    </el-drawer>
  </div>
</template>
