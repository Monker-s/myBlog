import { ElMessage } from "element-plus";

class NotificationWebSocket {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.baseReconnectDelay = 1000;
    this.userId = null;
    this.token = null;
  }

  connect(userId, token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log("[WS] 已存在活跃连接");
      return;
    }

    this.userId = userId;
    this.token = token;

    // 开发环境直接连接后端，生产环境使用相对路径
    const isDev = import.meta.env.DEV;
    const wsBaseUrl = isDev ? "ws://localhost:8081" : `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}`;
    // Token 通过查询参数传递
    const wsUrl = `${wsBaseUrl}/ws/notifications?token=${encodeURIComponent(token)}`;

    console.log("[WS] 正在连接:", wsUrl);

    try {
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log("[WS] 连接成功");
        this.reconnectAttempts = 0;
        this.startHeartbeat();
        this.emit("open");
      };

      this.ws.onmessage = (event) => {
        const data = this.parseMessage(event.data);
        console.log("[WS] 收到消息:", data);
        this.handleMessage(data);
      };

      this.ws.onerror = (error) => {
        console.error("[WS] 连接错误:", error);
        this.emit("error", error);
      };

      this.ws.onclose = (event) => {
        console.log("[WS] 连接关闭:", event.code, event.reason);
        this.stopHeartbeat();
        this.emit("close", event);
        this.reconnect();
      };
    } catch (error) {
      console.error("[WS] 创建连接失败:", error);
      this.reconnect();
    }
  }

  parseMessage(data) {
    try {
      return JSON.parse(data);
    } catch {
      return { type: "raw", payload: data };
    }
  }

  handleMessage(data) {
    if (data.type === "pong") {
      return;
    }

    if (data.type === "ping") {
      this.send({ type: "pong" });
      return;
    }

    if (data.type === "notification") {
      this.emit("notification", data.payload);
      this.showNotificationToast(data.payload);
    }

    this.emit("message", data);
  }

  showNotificationToast(notification) {
    const title = notification.title || "新通知";
    const content = notification.content || notification.message || "";

    ElMessage({
      message: `${title}: ${content}`,
      type: "info",
      duration: 5000,
      showClose: true,
    });
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      console.warn("[WS] 连接未就绪，消息发送失败");
    }
  }

  markAsRead(notificationId) {
    this.send({
      type: "mark_read",
      notificationId: notificationId,
    });
  }

  subscribe(types = []) {
    this.send({
      type: "subscribe",
      channels: types,
    });
  }

  unsubscribe(types = []) {
    this.send({
      type: "unsubscribe",
      channels: types,
    });
  }

  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: "ping" });
    }, 30000);
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  reconnect() {
    if (this.reconnectTimer || this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log("[WS] 停止重连");
      return;
    }

    this.reconnectAttempts++;
    const delay = this.baseReconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    console.log(`[WS] ${delay}ms 后尝试第 ${this.reconnectAttempts} 次重连`);

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.connect(this.userId, this.token);
    }, delay);
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }

  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event);
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach((callback) => {
        try {
          callback(data);
        } catch (error) {
          console.error(`[WS] 事件回调执行失败 [${event}]:`, error);
        }
      });
    }
  }

  disconnect() {
    this.stopHeartbeat();
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.reconnectAttempts = this.maxReconnectAttempts;

    if (this.ws) {
      this.ws.onclose = null;
      this.ws.close();
      this.ws = null;
    }

    console.log("[WS] 已断开连接");
  }

  get isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN;
  }
}

export const notificationWS = new NotificationWebSocket();
