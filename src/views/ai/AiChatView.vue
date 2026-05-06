<script setup>
import { setPageMeta } from "@/utils/seo";

setPageMeta({
  title: "AI 助手（占位） | 个人知识库",
  description: "预留 AI 对话入口，后续可接入模型能力。"
});
</script>

<template>
  <section class="page-panel ai-view">
    <h1>AI 助手（占位）</h1>
    <p>
      此页面用于保留未来扩展入口。当前版本建议优先完成文章管理、评论与通知链路。
    </p>

    <el-empty description="功能开发中" />
  </section>
</template>

<style scoped>
.ai-view h1 {
  margin: 0;
}

.ai-view p {
  color: var(--text-muted);
}
</style>