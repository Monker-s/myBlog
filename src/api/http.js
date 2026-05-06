import { applyRequestInterceptor, handleApiResponse } from "@/utils/service";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(
  /\/+$/,
  "",
);

export class ApiError extends Error {
  constructor(message, options = {}) {
    super(message);
    this.name = "ApiError";
    this.status = options.status ?? 500;
    this.code = options.code ?? this.status;
    this.data = options.data ?? null;
    this.requestId = options.requestId ?? null;
    this.timestamp = options.timestamp ?? null;
    this.url = options.url ?? null;
    this.response = options.response ?? null;
  }
}

export function buildQuery(query = {}) {
  const params = new URLSearchParams();

  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item === undefined || item === null || item === "") return;
        params.append(key, String(item));
      });
      return;
    }

    if (value instanceof Date) {
      params.append(key, value.toISOString());
      return;
    }

    params.append(key, String(value));
  });

  return params.toString();
}

export function buildApiUrl(path, query = {}) {
  if (!path) {
    throw new TypeError("buildApiUrl: path is required.");
  }

  const isAbsolute = /^https?:\/\//i.test(path);
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const basePath = isAbsolute ? path : `${API_BASE_URL}${normalizedPath}`;
  const url = new URL(basePath, window.location.origin);

  const search = buildQuery(query);
  if (search) {
    url.search = search;
  }

  return url.toString();
}

function normalizeEnvelope(payload, status, statusText) {
  if (
    payload &&
    typeof payload === "object" &&
    ("code" in payload || "message" in payload || "data" in payload)
  ) {
    return payload;
  }

  return {
    code: status,
    message: statusText || "",
    data: payload ?? null,
    requestId: null,
    timestamp: new Date().toISOString(),
  };
}

function createBody(body, headers) {
  if (body === undefined || body === null) return undefined;

  if (
    body instanceof FormData ||
    body instanceof URLSearchParams ||
    body instanceof Blob ||
    typeof body === "string"
  ) {
    return body;
  }

  if (!headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  return JSON.stringify(body);
}

async function parseResponse(response) {
  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  if (contentType.startsWith("text/") || contentType.includes("xml")) {
    return response.text();
  }

  return response.blob();
}

export async function request(path, options = {}) {
  const {
    method = "GET",
    query,
    body,
    headers = {},
    signal,
    credentials = "include",
  } = options;

  // 应用请求拦截器：添加 token
  const finalHeaders = applyRequestInterceptor(path, headers);
  const normalizedMethod = String(method).toUpperCase();
  const requestBody = createBody(body, finalHeaders);

  const response = await fetch(buildApiUrl(path, query), {
    method: normalizedMethod,
    headers: finalHeaders,
    body:
      normalizedMethod === "GET" || normalizedMethod === "HEAD"
        ? undefined
        : requestBody,
    credentials,
    signal,
  });

  const payload = await parseResponse(response);
  const envelope = normalizeEnvelope(
    payload,
    response.status,
    response.statusText,
  );
  const requestId =
    response.headers.get("x-request-id") || envelope.requestId || null;

  if (!response.ok) {
    throw new ApiError(envelope.message || `HTTP ${response.status}`, {
      status: response.status,
      code: envelope.code ?? response.status,
      data: envelope.data ?? payload,
      requestId,
      timestamp: envelope.timestamp ?? null,
      url: response.url,
      response: envelope,
    });
  }

  // 应用响应拦截器：处理业务状态码（code===0 表示成功）
  const result = await handleApiResponse({ ...envelope, requestId });
  return result;
}

export function get(path, options = {}) {
  return request(path, { ...options, method: "GET" });
}

export function post(path, options = {}) {
  return request(path, { ...options, method: "POST" });
}

export function put(path, options = {}) {
  return request(path, { ...options, method: "PUT" });
}

export function patch(path, options = {}) {
  return request(path, { ...options, method: "PATCH" });
}

export function del(path, options = {}) {
  return request(path, { ...options, method: "DELETE" });
}

export function unwrapData(envelope, fallback = null) {
  if (envelope && typeof envelope === "object" && "data" in envelope) {
    return envelope.data;
  }
  return envelope ?? fallback;
}
