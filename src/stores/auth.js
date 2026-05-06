import { defineStore } from "pinia";
import {
  getCurrentUser,
  login,
  logout,
  refreshSession,
  register,
} from "@/api/auth";
import { unwrapData } from "@/utils/service";
import { setToken, clearToken } from "@/utils/service";
import { ROLE_ADMIN } from "@/constants/roles";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    initialized: false,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.user?.id),
    isAdmin: (state) => Number(state.user?.role) === ROLE_ADMIN,
  },
  actions: {
    async restoreSession() {
      if (this.initialized) return;
      this.loading = true;
      try {
        await refreshSession();
        const me = await getCurrentUser();
        this.user = unwrapData(me);
      } catch {
        this.user = null;
      } finally {
        this.initialized = true;
        this.loading = false;
      }
    },

    async loginWithPassword(payload) {
      this.loading = true;
      try {
        const response = await login(payload);

        // 保存 token（假设后端返回的响应中包含 token）
        const token = response.data?.token || response.token;
        if (token) {
          setToken(token);
        }

        const me = await getCurrentUser();
        this.user = unwrapData(me);
        this.initialized = true; // 登录成功后标记为已初始化
        return this.user;
      } finally {
        this.loading = false;
      }
    },

    async registerUser(payload) {
      this.loading = true;
      try {
        await register(payload);
      } finally {
        this.loading = false;
      }
    },

    async logoutCurrentUser() {
      this.loading = true;
      try {
        await logout();
      } finally {
        clearToken();
        this.user = null;
        this.loading = false;
      }
    },
  },
});
