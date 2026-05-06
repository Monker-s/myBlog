import { onBeforeUnmount, ref } from "vue";
import { createNotificationsStream, startNotificationsPolling } from "@/api/notifications";

export function useSseNotifications({ onMessage, pollFallback }) {
  const stream = ref(null);
  const stopPolling = ref(null);

  const stop = () => {
    if (stream.value) {
      stream.value.close();
      stream.value = null;
    }
    if (stopPolling.value) {
      stopPolling.value();
      stopPolling.value = null;
    }
  };

  const start = () => {
    stop();

    try {
      stream.value = createNotificationsStream({
        onMessage,
        onError: () => {
          // SSE 异常时自动退化到轮询，满足文档补充条款
          if (!stopPolling.value && typeof pollFallback === "function") {
            stopPolling.value = startNotificationsPolling(pollFallback, { intervalMs: 30000 });
          }
        }
      });
    } catch {
      if (typeof pollFallback === "function") {
        stopPolling.value = startNotificationsPolling(pollFallback, { intervalMs: 30000 });
      }
    }
  };

  onBeforeUnmount(stop);

  return { start, stop, stream };
}