<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ROUTE_NAMES } from "@/constants/routes";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const navItems = [
  {
    name: ROUTE_NAMES.ADMIN_HOME,
    label: "后台首页",
    hint: "概览和快速操作",
  },
  {
    name: ROUTE_NAMES.ADMIN_POSTS,
    label: "文章管理",
    hint: "草稿 / 发布 / 编辑",
  },
  {
    name: ROUTE_NAMES.ADMIN_CATEGORIES,
    label: "分类管理",
    hint: "文章分类维护",
  },
  {
    name: ROUTE_NAMES.ADMIN_TAGS,
    label: "标签管理",
    hint: "文章标签维护",
  },
  {
    name: ROUTE_NAMES.ADMIN_USERS,
    label: "用户管理",
    hint: "角色与状态",
  },
  {
    name: ROUTE_NAMES.ADMIN_DECORATIONS,
    label: "装饰配置",
    hint: "Logo / 文案 / 主题色",
  },
];

const activeName = computed(() => String(route.name || ""));

const handleLogout = async () => {
  try {
    await authStore.logoutCurrentUser();
    ElMessage.success("已退出登录");
    await router.replace({ name: ROUTE_NAMES.AUTH });
  } catch {
    ElMessage.error("退出失败，请稍后重试");
  }
};
</script>

<template>
  <div class="admin-background">
    <section class="admin-shell">
      <header class="admin-header">
        <div>
          <p class="signal">ADMIN DASHBOARD</p>
          <h1>管理后台控制台</h1>
        </div>
        <div class="header-actions">
          <router-link class="back-link" to="/">返回前台</router-link>
          <button type="button" class="logout-link" @click="handleLogout">
            退出登录
          </button>
        </div>
      </header>

      <div class="admin-grid">
        <aside class="admin-nav" aria-label="后台导航">
          <h2>功能模块</h2>
          <router-link
            v-for="item in navItems"
            :key="item.name"
            :to="{ name: item.name }"
            class="nav-item"
            :class="{ active: activeName === item.name }"
          >
            <strong>{{ item.label }}</strong>
            <small>{{ item.hint }}</small>
          </router-link>
        </aside>

        <main class="admin-main">
          <router-view />
        </main>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-background {
  background: #f8fafc;
  min-height: 100vh;
  padding: 16px 0;
}

.admin-shell {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
  display: grid;
  gap: 14px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.signal {
  margin: 0;
  color: #6366f1;
  font-size: 12px;
  letter-spacing: 0.14em;
  font-weight: 600;
}

.admin-header h1 {
  margin: 6px 0 0;
  font-family: "Cinzel", serif;
  font-size: 24px;
  color: #1f2937;
}

.back-link,
.logout-link {
  text-decoration: none;
  color: #4b5563;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.back-link:hover {
  background: #f9fafb;
}

.back-link:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logout-link {
  cursor: pointer;
  font: inherit;
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  color: #dc2626;
  border-color: #fecaca;
}

.logout-link:hover {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
}

.admin-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: 290px minmax(0, 1fr);
  align-items: start;
}

.admin-nav {
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px;
  display: grid;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.admin-nav h2 {
  margin: 0 0 8px;
  font-family: "Cinzel", serif;
  font-size: 16px;
  color: #1f2937;
  padding-bottom: 12px;
  border-bottom: 2px solid #f3f4f6;
}

.nav-item {
  text-decoration: none;
  color: #4b5563;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  display: grid;
  gap: 4px;
  transition: all 0.2s ease;
  background: #f9fafb;
}

.nav-item small {
  color: #9ca3af;
  font-size: 12px;
}

.nav-item.active,
.nav-item:hover {
  border-color: #6366f1;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #4338ca;
}

.nav-item.active small,
.nav-item:hover small {
  color: #6366f1;
}

.admin-main {
  min-width: 0;
}

@media (max-width: 980px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>
