import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";

export function useAuthGuard() {
  const authStore = useAuthStore();

  const isAuthenticated = computed(() => authStore.isAuthenticated);
  const isAdmin = computed(() => authStore.isAdmin);

  return {
    isAuthenticated,
    isAdmin
  };
}