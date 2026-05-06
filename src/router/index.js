import { createRouter, createWebHistory } from "vue-router";
import { ROUTE_NAMES } from "@/constants/routes";
import { useAuthStore } from "@/stores/auth";

const routes = [
  {
    path: "/",
    component: () => import("@/layouts/AppLayout.vue"),
    children: [
      {
        path: "",
        name: ROUTE_NAMES.HOME,
        component: () => import("@/views/HomeView.vue"),
      },
      {
        path: "articles",
        name: ROUTE_NAMES.ARTICLES,
        component: () => import("@/views/articles/ArticlesListView.vue"),
      },
      {
        path: "notifications",
        name: ROUTE_NAMES.NOTIFICATIONS,
        component: () => import("@/views/notifications/NotificationsView.vue"),
        meta: { requiresAuth: true },
      },
      {
        path: "ai-chat",
        name: ROUTE_NAMES.AI_CHAT,
        component: () => import("@/views/ai/AiChatView.vue"),
      },
      {
        path: "profile",
        name: ROUTE_NAMES.USER_PROFILE,
        component: () => import("@/views/user/UserProfileView.vue"),
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: "/auth",
    name: ROUTE_NAMES.AUTH,
    component: () => import("@/views/auth/AuthView.vue"),
  },
  {
    // 详情页独立为顶级路由，不再嵌在首页布局中
    path: "/posts/:id",
    name: ROUTE_NAMES.ARTICLE_DETAIL,
    component: () => import("@/views/articles/ArticleDetailView.vue"),
    props: true,
  },
  {
    // 兼容旧链接：/article/:id -> /posts/:id
    path: "/article/:id",
    redirect: (to) => ({
      name: ROUTE_NAMES.ARTICLE_DETAIL,
      params: { id: to.params.id },
      query: to.query,
    }),
  },
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: "",
        name: ROUTE_NAMES.ADMIN_HOME,
        component: () => import("@/views/admin/AdminHomeView.vue"),
      },
      {
        path: "posts",
        name: ROUTE_NAMES.ADMIN_POSTS,
        component: () => import("@/views/admin/AdminPostsView.vue"),
      },
      {
        path: "categories",
        name: ROUTE_NAMES.ADMIN_CATEGORIES,
        component: () => import("@/views/admin/AdminCategoriesView.vue"),
      },
      {
        path: "tags",
        name: ROUTE_NAMES.ADMIN_TAGS,
        component: () => import("@/views/admin/AdminTagsView.vue"),
      },
      {
        path: "users",
        name: ROUTE_NAMES.ADMIN_USERS,
        component: () => import("@/views/admin/AdminUsersView.vue"),
      },
      {
        path: "decorations",
        name: ROUTE_NAMES.ADMIN_DECORATIONS,
        component: () => import("@/views/admin/AdminDecorationsView.vue"),
      },
      {
        path: "uv",
        name: ROUTE_NAMES.ADMIN_UV,
        component: () => import("@/views/admin/AdminDashboardView.vue"),
      },
    ],
  },
  {
    path: "/admin/posts/create",
    name: ROUTE_NAMES.ADMIN_POST_CREATE,
    component: () => import("@/views/admin/AdminPostEditView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/posts/:id/edit",
    name: ROUTE_NAMES.ADMIN_POST_EDIT,
    component: () => import("@/views/admin/AdminPostEditView.vue"),
    props: true,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();

  // 首次进入页面前尝试恢复会话，避免刷新后误判为未登录
  if (!authStore.initialized) {
    await authStore.restoreSession();
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: ROUTE_NAMES.AUTH,
      query: { redirect: to.fullPath },
    };
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: ROUTE_NAMES.HOME };
  }

  if (to.name === ROUTE_NAMES.AUTH && authStore.isAuthenticated) {
    const redirect =
      typeof to.query.redirect === "string" ? to.query.redirect : "/";
    return redirect;
  }

  return true;
});

export default router;
