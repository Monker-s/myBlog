<script setup>
const props = defineProps({
  siteName: {
    type: String,
    default: "个人知识库"
  }
});
</script>

<template>
  <footer class="app-footer panel-surface" role="contentinfo">
    <img src="/images/imperial-eagle.webp" alt="帝国徽记" />
    <p>© {{ new Date().getFullYear() }} {{ props.siteName }} · The Codex Endures</p>
  </footer>
</template>

<style scoped>
.app-footer {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-align: center;
  padding: 12px;
  color: var(--text-muted);
  border-color: var(--border-strong);
}

.app-footer img {
  width: 24px;
  height: 24px;
  object-fit: cover;
  border-radius: 6px;
}
</style>