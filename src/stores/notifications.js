import { defineStore } from "pinia";
import {
  getNotifications,
  getUnreadNotifications,
  markNotificationsRead
} from "@/api/notifications";
import { unwrapData } from "@/utils/service";

function normalizeList(payload) {
  if (Array.isArray(payload)) {
    return { list: payload, total: payload.length };
  }

  if (payload && Array.isArray(payload.list)) {
    return {
      list: payload.list,
      total: Number(payload.total ?? payload.list.length)
    };
  }

  return { list: [], total: 0 };
}

export const useNotificationStore = defineStore("notifications", {
  state: () => ({
    list: [],
    unreadList: [],
    unreadCount: 0,
    total: 0,
    loading: false
  }),
  actions: {
    async fetchUnread() {
      this.loading = true;
      try {
        const res = await getUnreadNotifications({ page: 1, pageSize: 100 });
        const data = normalizeList(unwrapData(res));
        this.unreadList = data.list;
        this.unreadCount = data.list.length;
      } catch {
        // 后端未就绪时保持静默降级，避免页面崩溃
      } finally {
        this.loading = false;
      }
    },

    async fetchList(params = {}) {
      this.loading = true;
      try {
        const res = await getNotifications(params);
        const data = normalizeList(unwrapData(res));
        this.list = data.list;
        this.total = data.total;
      } finally {
        this.loading = false;
      }
    },

    async markRead(ids = []) {
      if (!ids.length) return;
      await markNotificationsRead({ ids });
      this.list = this.list.map((item) => (ids.includes(item.id) ? { ...item, is_read: 1 } : item));
      this.unreadList = this.unreadList.filter((item) => !ids.includes(item.id));
      this.unreadCount = this.unreadList.length;
    },

    async markAllRead() {
      const ids = this.unreadList.map((item) => item.id).filter(Boolean);
      if (!ids.length) return;
      await this.markRead(ids);
    }
  }
});
