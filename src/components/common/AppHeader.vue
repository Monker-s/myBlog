<script setup>
import { computed } from "vue";
import NotificationBell from "@/components/notifications/NotificationBell.vue";
import { ROUTE_NAMES } from "@/constants/routes";

const props = defineProps({
  siteName: {
    type: String,
    default: "个人知识库"
  },
  siteSlogan: {
    type: String,
    default: ""
  },
  user: {
    type: Object,
    default: null
  },
  unreadCount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(["toggle-sidebar", "logout"]);

const isAdminUser = computed(() => Number(props.user?.role) === 99);
</script>

<template>
  <header class="app-header panel-surface" role="banner">
    <div class="left-group">
      <el-button class="mobile-menu" text @click="emit('toggle-sidebar')">目录</el-button>

      <router-link :to="{ name: ROUTE_NAMES.HOME }" class="brand-link">
        <img src="/images/imperial-eagle.webp" alt="帝国双头鹰徽记" class="brand-emblem" />
        <div>
          <strong class="brand-title">{{ props.siteName }}</strong>
          <small>{{ props.siteSlogan || "Knowledge Never Sleeps" }}</small>
        </div>
      </router-link>
    </div>

    <nav class="top-nav" aria-label="主导航">
      <router-link :to="{ name: ROUTE_NAMES.HOME }">舰桥</router-link>
      <router-link :to="{ name: ROUTE_NAMES.ARTICLES }">圣典</router-link>
      <router-link :to="{ name: ROUTE_NAMES.AI_CHAT }">机魂</router-link>
      <router-link :to="{ name: ROUTE_NAMES.NOTIFICATIONS }" class="notice-link">
        <NotificationBell :count="props.unreadCount" />
      </router-link>

      <template v-if="props.user">
        <router-link v-if="isAdminUser" :to="{ name: ROUTE_NAMES.ADMIN_POSTS }" class="auth-link">
          后台
        </router-link>
        <span class="auth-link user-name">指挥官：{{ props.user.username || "用户" }}</span>
        <!-- 退出入口：统一交给父布局执行登出与跳转 -->
        <button type="button" class="auth-link logout-link" @click="emit('logout')">退出</button>
      </template>

      <router-link v-else :to="{ name: ROUTE_NAMES.AUTH }" class="auth-link">登录/注册</router-link>
    </nav>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
  border-color: var(--border-strong);
}

.left-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}

.brand-emblem {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 9px;
  border: 1px solid var(--border-strong);
  box-shadow: 0 0 0 2px rgba(10, 18, 30, 0.8);
}

.brand-title {
  font-size: 15px;
  color: #eef4ff;
}

.brand-link small {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.top-nav a,
.top-nav .auth-link,
.top-nav .logout-link {
  text-decoration: none;
  color: #dae6ff;
  border: 1px solid rgba(89, 118, 168, 0.46);
  border-radius: 6px;
  padding: 5px 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: linear-gradient(145deg, rgba(7, 17, 31, 0.9), rgba(12, 22, 40, 0.64));
  transition: 0.25s ease;
}

.top-nav a:hover,
.top-nav a.router-link-active,
.top-nav .auth-link:hover,
.top-nav .logout-link:hover {
  color: #fff;
  border-color: var(--accent-color);
  background: linear-gradient(145deg, rgba(47, 99, 217, 0.38), rgba(10, 20, 34, 0.35));
  box-shadow:
    inset 0 0 12px rgba(31, 210, 255, 0.26),
    0 0 18px rgba(31, 210, 255, 0.18);
}

.top-nav .logout-link {
  cursor: pointer;
  font: inherit;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.top-nav .user-name {
  cursor: default;
}

.top-nav .user-name:hover {
  border-color: rgba(89, 118, 168, 0.46);
  background: linear-gradient(145deg, rgba(7, 17, 31, 0.9), rgba(12, 22, 40, 0.64));
  box-shadow: none;
}

.mobile-menu {
  display: none;
}

@media (max-width: 1080px) {
  .mobile-menu {
    display: inline-flex;
    color: #e4ecff;
  }

  .brand-link small {
    display: none;
  }
}
</style>
