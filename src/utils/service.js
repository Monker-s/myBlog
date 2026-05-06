import axios from "axios";
import { ElMessage } from "element-plus";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(
  /\/+$/,
  "",
);

const TOKEN_STORAGE_KEY = "auth_token";
const TOKEN_BYPASS_PATHS = [
  "/api/auth/login",
  "/api/auth/register",
  "/api/auth/forgot",
  "/api/auth/forgot-password",
  "/api/auth/reset-password",
  "/api/auth/logout",
  "/api/auth/sendCode",
];

function normalizePath(url = "") {
  if (!url) return "";
  try {
    if (/^https?:\/\//i.test(url)) {
      return new URL(url).pathname;
    }
    if (typeof window !== "undefined") {
      return new URL(url, window.location.origin).pathname;
    }
  } catch {
    // Ignore parse errors and fall back to string split.
  }
  return String(url).split("?")[0];
}

function isBypassPath(pathname) {
  return TOKEN_BYPASS_PATHS.some((path) => pathname.endsWith(path));
}

export function setToken(token) {
  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_STORAGE_KEY, token || "");
  }
}

export function getToken() {
  if (typeof window !== "undefined") {
    return localStorage.getItem(TOKEN_STORAGE_KEY) || "";
  }
  return "";
}

export function clearToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }
}

export function needTokenValidation(path, method = "get") {
  const normalizedMethod = String(method || "get").toLowerCase();
  const pathname = normalizePath(path);

  // 白名单路径不需要 token
  if (isBypassPath(pathname)) return false;

  // 其他所有请求（包括 GET）都需要 token
  return true;
}

function createBusinessError(payload, fallbackMessage = "Request failed") {
  const error = new Error(payload?.message || fallbackMessage);
  error.name = "BusinessError";
  error.code = payload?.code ?? -1;
  error.data = payload?.data ?? null;
  error.requestId = payload?.requestId ?? null;
  error.timestamp = payload?.timestamp ?? null;
  error.response = payload ?? null;
  return error;
}

let redirectingToAuth = false;

function handleTokenExpired(payload) {
  clearToken();
  if (typeof window === "undefined") return;

  ElMessage.warning(payload?.message || "Session expired, please login again.");

  if (redirectingToAuth) return;
  redirectingToAuth = true;
  setTimeout(() => {
    if (window.location.pathname !== "/auth") {
      window.location.href = "/auth";
      return;
    }
    redirectingToAuth = false;
  }, 300);
}

export function applyRequestInterceptor(path, headers = {}, method = "post") {
  const finalHeaders = { ...headers };
  if (!needTokenValidation(path, method)) {
    return finalHeaders;
  }

  const token = getToken();
  if (token) {
    finalHeaders.Authorization = `Bearer ${token}`;
  }
  return finalHeaders;
}

export function handleApiResponse(payload) {
  if (payload && typeof payload === "object" && "code" in payload) {
    if (Number(payload.code) === 0) return payload;

    if (Number(payload.code) === 401 || Number(payload.code) === 403) {
      handleTokenExpired(payload);
    }
    return Promise.reject(createBusinessError(payload));
  }
  return payload;
}

export function buildApiUrl(path, query = {}) {
  if (!path) {
    throw new TypeError("buildApiUrl: path is required.");
  }

  const isAbsolute = /^https?:\/\//i.test(path);
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const basePath = isAbsolute ? path : `${API_BASE_URL}${normalizedPath}`;
  const baseOrigin =
    typeof window === "undefined" ? "http://localhost" : window.location.origin;
  const url = new URL(basePath, baseOrigin);

  const params = new URLSearchParams();
  Object.entries(query || {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item === undefined || item === null || item === "") return;
        params.append(key, String(item));
      });
      return;
    }
    params.append(key, String(value));
  });

  const search = params.toString();
  if (search) {
    url.search = search;
  }
  return url.toString();
}

export function unwrapData(envelope, fallback = null) {
  if (envelope && typeof envelope === "object" && "data" in envelope) {
    return envelope.data;
  }
  return envelope ?? fallback;
}

const service = axios.create({
  baseURL: API_BASE_URL || undefined,
  timeout: 20000,
  withCredentials: true,
});

service.interceptors.request.use(
  (config) => {
    const method = String(config.method || "get").toLowerCase();
    const pathname = normalizePath(config.url || "");

    if (needTokenValidation(pathname, method)) {
      const token = getToken();
      if (!token) {
        return Promise.reject(
          createBusinessError(
            {
              code: 401,
              message: "Missing token, please login first.",
            },
            "Missing token, please login first.",
          ),
        );
      }

      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

service.interceptors.response.use(
  (response) => {
    const payload = response?.data;

    if (payload && typeof payload === "object" && "code" in payload) {
      if (Number(payload.code) === 0) {
        return payload;
      }

      if (Number(payload.code) === 401 || Number(payload.code) === 403) {
        handleTokenExpired(payload);
      }
      return Promise.reject(createBusinessError(payload));
    }

    return payload;
  },
  (error) => {
    const payload = error?.response?.data;
    if (payload && typeof payload === "object" && "code" in payload) {
      if (Number(payload.code) === 401 || Number(payload.code) === 403) {
        handleTokenExpired(payload);
      }
      return Promise.reject(createBusinessError(payload));
    }

    if (error?.response?.status === 401 || error?.response?.status === 403) {
      handleTokenExpired({
        code: error.response.status,
        message: "Session expired, please login again.",
      });
    }

    return Promise.reject(error);
  },
);

export default service;
