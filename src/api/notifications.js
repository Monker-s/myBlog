import service, { buildApiUrl } from "@/utils/service";

// 统一使用 /api/notifications 接口获取所有通知
export function getNotifications(params = {}) {
  const { page = 1, pageSize = 20 } = params;
  return service.get("/api/notifications", {
    params: { page, pageSize },
  });
}

// 获取未读通知（先获取所有，前端筛选未读）
export function getUnreadNotifications(params = {}) {
  return getNotifications(params);
}

export function markNotificationsRead(notificationIds) {
  return service.put("/api/notifications/read", { notificationIds });
}

export function markNotificationRead(notificationId) {
  return markNotificationsRead([notificationId]);
}

export function getNotificationDetail(notificationId) {
  return service.get(`/api/notifications/${notificationId}`);
}

// 管理员群发通知
export function sendBroadcastNotification(data) {
  return service.post("/api/admin/notifications", data);
}

export function createNotificationsStream(options = {}) {
  if (typeof EventSource === "undefined") {
    throw new Error("EventSource is not supported in this runtime.");
  }

  const { lastEventId, onOpen, onMessage, onError } = options;
  const streamUrl = buildApiUrl("/api/notifications/stream", { lastEventId });
  const stream = new EventSource(streamUrl, { withCredentials: true });

  if (typeof onOpen === "function") {
    stream.onopen = onOpen;
  }

  if (typeof onError === "function") {
    stream.onerror = onError;
  }

  if (typeof onMessage === "function") {
    stream.onmessage = (event) => {
      let data = event.data;
      try {
        data = JSON.parse(event.data);
      } catch {
        // Keep raw event string when payload is not JSON.
      }
      onMessage(data, event);
    };
  }

  return stream;
}

export function startNotificationsPolling(fetcher, options = {}) {
  const { intervalMs = 30000, immediate = true } = options;
  let timerId = null;
  let stopped = false;

  const tick = async () => {
    if (stopped) return;

    try {
      await fetcher();
    } finally {
      if (!stopped) {
        timerId = setTimeout(tick, intervalMs);
      }
    }
  };

  if (immediate) {
    void tick();
  } else {
    timerId = setTimeout(tick, intervalMs);
  }

  return () => {
    stopped = true;
    if (timerId) {
      clearTimeout(timerId);
    }
  };
}
