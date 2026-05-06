import { ref } from "vue";

export function useDebounce(defaultDelay = 300) {
  const timer = ref(null);

  // 用于输入检索场景，减少连续触发请求
  const run = (callback, delay = defaultDelay) => {
    if (timer.value) clearTimeout(timer.value);
    timer.value = setTimeout(() => callback(), delay);
  };

  const cancel = () => {
    if (timer.value) {
      clearTimeout(timer.value);
      timer.value = null;
    }
  };

  return { run, cancel };
}