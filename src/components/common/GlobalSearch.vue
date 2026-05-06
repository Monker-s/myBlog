<script setup>
import { ref } from "vue";
import { useDebounce } from "@/composables/useDebounce";

const emit = defineEmits(["search"]);
const keyword = ref("");
const { run } = useDebounce(300);

const onInput = () => {
  run(() => emit("search", keyword.value.trim()));
};
</script>

<template>
  <label class="global-search">
    <span class="sr-only">全局搜索</span>
    <input
      v-model="keyword"
      type="search"
      placeholder="搜索战术文献..."
      @input="onInput"
    />
  </label>
</template>

<style scoped>
.global-search input {
  width: 100%;
  height: 40px;
  border: 1px solid rgba(116, 151, 210, 0.4);
  border-radius: 12px;
  padding: 0 12px;
  background: rgba(5, 13, 24, 0.8);
  color: #e3edff;
  transition: 0.2s ease;
}

.global-search input::placeholder {
  color: #8199bd;
}

.global-search input:focus {
  border-color: var(--accent-color);
  box-shadow: 0 0 0 2px rgba(212, 166, 59, 0.24);
}
</style>