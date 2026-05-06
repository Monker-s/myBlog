<script setup>
const props = defineProps({
  toc: {
    type: Array,
    default: () => []
  }
});

const jumpTo = (id) => {
  const target = document.getElementById(id);
  if (target) {
    target.scrollIntoView({ behavior: "smooth", block: "start" });
  }
};
</script>

<template>
  <nav class="toc page-panel" aria-label="文章目录">
    <h3>目录</h3>
    <ul>
      <li v-for="item in props.toc" :key="item.id" :class="`level-${item.level}`">
        <button type="button" @click="jumpTo(item.id)">{{ item.text }}</button>
      </li>
    </ul>
  </nav>
</template>

<style scoped>
.toc h3 {
  margin-top: 0;
}

.toc ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.toc li.level-3 {
  padding-left: 12px;
}

.toc li.level-4 {
  padding-left: 24px;
}

.toc button {
  border: 0;
  background: transparent;
  color: var(--primary-color);
  cursor: pointer;
  padding: 0;
  text-align: left;
}
</style>
