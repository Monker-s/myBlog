import service from "@/utils/service";

/**
 * 仅用于前端联调：当后端认证接口不可用时，允许用固定管理员账号进入后台。
 * 注意：默认只在开发环境启用；生产环境不会走该分支。
 */
const ENABLE_DEV_AUTH_FALLBACK =
  Boolean(import.meta.env.DEV) ||
  String(import.meta.env.VITE_AUTH_MOCK || "").toLowerCase() === "true";

const MOCK_SESSION_KEY = "__kb_auth_mock_session__";

export const MOCK_ADMIN_CREDENTIAL = Object.freeze({
  username: "admin",
  password: "Admin@123456",
});

function buildMockAdminProfile() {
  return {
    id: 99,
    username: "admin",
    email: "admin@example.com",
    role: 99,
    status: 1,
    last_login_at: new Date().toISOString(),
  };
}

function createEnvelope(data = null, message = "ok") {
  return {
    code: 0,
    message,
    data,
    requestId: "mock-auth",
    timestamp: new Date().toISOString(),
  };
}

function canUseSessionStorage() {
  return (
    typeof window !== "undefined" &&
    typeof window.sessionStorage !== "undefined"
  );
}

function saveMockSessionUser(user) {
  if (!canUseSessionStorage()) return;
  try {
    if (!user) {
      window.sessionStorage.removeItem(MOCK_SESSION_KEY);
      return;
    }
    window.sessionStorage.setItem(MOCK_SESSION_KEY, JSON.stringify(user));
  } catch {
    // 忽略存储异常，避免影响主流程
  }
}

function loadMockSessionUser() {
  if (!canUseSessionStorage()) return null;
  try {
    const raw = window.sessionStorage.getItem(MOCK_SESSION_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? parsed : null;
  } catch {
    return null;
  }
}

let mockSessionUser = loadMockSessionUser();

function isMockAdminLogin(payload = {}) {
  const username = String(payload.username || "").trim();
  const password = String(payload.password || "");
  return (
    username === MOCK_ADMIN_CREDENTIAL.username &&
    password === MOCK_ADMIN_CREDENTIAL.password
  );
}

function normalizeEnvelopeData(response) {
  if (!response || typeof response !== "object") return null;
  if (!("data" in response)) return response;
  return response.data;
}

function isValidAuthUserPayload(payload) {
  if (!payload || typeof payload !== "object" || Array.isArray(payload))
    return false;
  const hasUserId = Number.isFinite(Number(payload.id));
  const hasUsername =
    typeof payload.username === "string" && payload.username.trim().length > 0;
  return hasUserId && hasUsername;
}

export async function login(payload) {
  try {
    const response = await service.post("/api/auth/login", payload);

    /**
     * 某些开发环境会把 /api/* 回落到 index.html，形成“200 但非 JSON 业务响应”。
     * 对于本地 mock 管理员登录，检测到异常结构后直接切回 mock 会话，避免后续守卫误判。
     */
    if (ENABLE_DEV_AUTH_FALLBACK && isMockAdminLogin(payload)) {
      const data = normalizeEnvelopeData(response);
      if (!data || typeof data === "string") {
        mockSessionUser = buildMockAdminProfile();
        saveMockSessionUser(mockSessionUser);
        return createEnvelope(null, "mock login success");
      }
    }

    return response;
  } catch (error) {
    if (!ENABLE_DEV_AUTH_FALLBACK || !isMockAdminLogin(payload)) {
      throw error;
    }

    mockSessionUser = buildMockAdminProfile();
    saveMockSessionUser(mockSessionUser);
    return createEnvelope(null, "mock login success");
  }
}

export function register(payload) {
  return service.post("/api/auth/register", payload);
}

/**
 * 发送邮箱验证码
 * @param {Object} payload
 * @param {string} payload.email - 邮箱地址
 * @param {string} payload.scene - 使用场景（register 或 forgot）
 */
export function sendCode(payload) {
  return service.post("/api/auth/sendCode", null, {
    params: {
      code: payload.code,
      scene: payload.scene,
    },
  });
}

/**
 * 忘记密码重置接口（按接口文档使用 Query 参数传递）。
 * 可通过 VITE_AUTH_RESET_PASSWORD_PATH 覆盖默认路由。
 */
export function resetPassword(payload = {}) {
  const endpoint =
    String(import.meta.env.VITE_AUTH_RESET_PASSWORD_PATH || "").trim() ||
    "/api/auth/forgot";

  return service.post(endpoint, null, {
    params: {
      email: payload.email,
      code: payload.code,
      password: payload.password,
      agPassword: payload.agPassword,
    },
  });
}

export async function logout(payload = {}) {
  try {
    return await service.post("/api/auth/logout", payload);
  } catch (error) {
    if (!ENABLE_DEV_AUTH_FALLBACK || !mockSessionUser) {
      throw error;
    }
    return createEnvelope(null, "mock logout success");
  } finally {
    mockSessionUser = null;
    saveMockSessionUser(null);
  }
}

export async function refreshSession(payload = {}) {
  try {
    return await service.post("/api/auth/refresh", payload);
  } catch (error) {
    if (!ENABLE_DEV_AUTH_FALLBACK || !mockSessionUser) {
      throw error;
    }
    return createEnvelope(null, "mock refresh success");
  }
}

export async function getCurrentUser() {
  try {
    const response = await service.get("/api/auth/me");
    const data = normalizeEnvelopeData(response);

    // 接口返回非用户对象时，视为无效响应；若存在 mock 会话则优先回退，保证可进入后台。
    if (
      !isValidAuthUserPayload(data) &&
      ENABLE_DEV_AUTH_FALLBACK &&
      mockSessionUser
    ) {
      return createEnvelope(mockSessionUser, "mock current user");
    }

    return response;
  } catch (error) {
    if (!ENABLE_DEV_AUTH_FALLBACK || !mockSessionUser) {
      throw error;
    }
    return createEnvelope(mockSessionUser, "mock current user");
  }
}

export async function updateUserInfo(payload) {
  try {
    return await service.post("/api/auth/update", payload);
  } catch (error) {
    if (!ENABLE_DEV_AUTH_FALLBACK || !mockSessionUser) {
      throw error;
    }
    // Mock 响应
    return createEnvelope(null, "mock update success");
  }
}
