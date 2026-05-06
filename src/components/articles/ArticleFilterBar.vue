<script setup>
import { reactive, watch } from "vue";

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  categories: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(["update:modelValue", "search"]);

const local = reactive({
  keyword: props.modelValue.keyword || "",
  category: props.modelValue.category || "",
  sort: props.modelValue.sort || "latest"
});

watch(
  () => ({ ...local }),
  (val) => {
    emit("update:modelValue", val);
  },
  { deep: true }
);
</script>

<template>
  <section class="filter-bar page-panel" aria-label="文章筛选">
    <el-form inline>
      <el-form-item label="关键词">
        <el-input v-model="local.keyword" clearable placeholder="输入标题或摘要关键词" />
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="local.category" clearable placeholder="全部分类" style="width: 160px">
          <el-option label="全部" value="" />
          <el-option
            v-for="item in props.categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="排序">
        <el-segmented
          v-model="local.sort"
          :options="[
            { label: '最新', value: 'latest' },
            { label: '最热', value: 'hot' },
            { label: '最多赞', value: 'likes' }
          ]"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="warning" @click="emit('search')">执行检索</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<style scoped>
.filter-bar {
  border-color: var(--border-strong);
}

.filter-bar :deep(.el-form-item__label) {
  color: #d9e5fb;
}

.filter-bar :deep(.el-input__wrapper),
.filter-bar :deep(.el-select__wrapper) {
  background: rgba(7, 15, 28, 0.8);
  box-shadow: inset 0 0 0 1px rgba(131, 166, 220, 0.35);
}

.filter-bar :deep(.el-input__inner),
.filter-bar :deep(.el-select__selected-item) {
  color: #e8f0ff;
}
</style>