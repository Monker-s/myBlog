import { defineStore } from "pinia";
import { getDecorationConfig } from "@/api/config";
import { unwrapData } from "@/utils/service";

const defaultDecoration = {
  siteName: "个人知识库",
  slogan: "记录构建路径，而不只是结论",
  logoText: "KB",
  primary: "#1e3a8a",
  accent: "#d4af37"
};

export const useAppStore = defineStore("app", {
  state: () => ({
    decoration: defaultDecoration,
    loaded: false
  }),
  actions: {
    async loadDecorationConfig() {
      if (this.loaded) return;
      try {
        const res = await getDecorationConfig();
        const config = unwrapData(res);
        if (config && typeof config === "object") {
          this.decoration = { ...defaultDecoration, ...config };
        }
      } catch {
        // 配置接口失败时使用本地默认主题
      } finally {
        this.loaded = true;
      }
    }
  }
});
