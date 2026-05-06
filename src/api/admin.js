import service from "@/utils/service";

function ensureId(name, value) {
  if (value === undefined || value === null || value === "") {
    throw new TypeError(`${name} is required.`);
  }
  return value;
}

export function getAdminPosts(params = {}) {
  const {
    keyword,
    status,
    category,
    authorId,
    page = 1,
    pageSize = 20,
    sort = "updated_at:desc",
  } = params;

  return service.get("/api/admin/posts", {
    params: { keyword, status, category, authorId, page, pageSize, sort },
  });
}

export function createAdminPost(payload) {
  return service.post("/api/admin/posts", payload);
}

export function updateAdminPost(postId, payload, options = {}) {
  const { useBodyId = false } = options;
  const id = ensureId("postId", postId);

  // 兼容两种后端路由风格：
  // 1) PUT /api/admin/posts/:id
  // 2) PUT /api/admin/posts (body.id)
  if (useBodyId) {
    return service.put("/api/admin/posts", { id, ...payload });
  }

  return service.put(`/api/admin/posts/${id}`, payload);
}

export function deleteAdminPost(postId) {
  const id = ensureId("postId", postId);
  return service.delete(`/api/admin/posts/${id}`);
}

export function getAdminPostDetail(postId) {
  const id = ensureId("postId", postId);
  return service.get(`/api/admin/posts/${id}`);
}

export function getAdminUsers(params = {}) {
  const { keyword, role, status, page = 1, pageSize = 20 } = params;
  return service.get("/api/admin/users", {
    params: { keyword, role, status, page, pageSize },
  });
}

export function updateAdminUserRole(userId, role, options = {}) {
  const id = ensureId("userId", userId);
  const { useBodyId = false } = options;

  if (useBodyId) {
    return service.put("/api/admin/users/role", { id, role });
  }

  return service.put(`/api/admin/users/${id}/role`, { role });
}

export function updateAdminUserStatus(userId, status) {
  const id = ensureId("userId", userId);
  return service.put(`/api/admin/users/${id}/status`, { status });
}

export function getAdminUvStats(params = {}) {
  const { days = 30, from, to } = params;
  return service.get("/api/admin/status/uv", {
    params: { days, from, to },
  });
}

export function getAdminPostStats() {
  return service.get("/api/admin/status/posts");
}

export function getAdminUserStats() {
  return service.get("/api/admin/status/users");
}

export function uploadFile(formData) {
  return service.post("/api/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}
