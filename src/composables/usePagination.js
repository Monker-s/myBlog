import { computed, ref } from "vue";

export function usePagination(defaultPage = 1, defaultPageSize = 10) {
  const page = ref(defaultPage);
  const pageSize = ref(defaultPageSize);
  const total = ref(0);

  const offset = computed(() => (page.value - 1) * pageSize.value);

  const resetPage = () => {
    page.value = 1;
  };

  return {
    page,
    pageSize,
    total,
    offset,
    resetPage
  };
}