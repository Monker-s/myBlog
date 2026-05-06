import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "node:path";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  server: {
    proxy: {
      // API 代理
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true,
      },
      // WebSocket 代理
      "/ws": {
        target: "ws://localhost:8081",
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
