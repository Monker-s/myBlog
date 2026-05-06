import service from "@/utils/service";

function ensureId(name, value) {
  if (value === undefined || value === null || value === "") {
    throw new TypeError(`${name} is required.`);
  }
  return value;
}

export function getPosts(params = {}) {
  const {
    keyword,
    category,
    categoryId,
    tags,
    sort = "latest",
    pinned,
    page = 1,
    pageSize = 10,
    status,
  } = params;

  return service.get("/api/posts", {
    params: {
      keyword,
      category: category ?? categoryId,
      sort,
      pinned,
      page,
      pageSize,
      status,
      tags,
    },
  });
}

export function getPostDetail(postId) {
  return service.get(`/api/posts/${ensureId("postId", postId)}`);
}

export function getPostLikeStatus(postId) {
  return service.get(`/api/posts/${ensureId("postId", postId)}/like`);
}

export function likePost(postId) {
  return service.post(`/api/posts/${ensureId("postId", postId)}/like`);
}

export function reportPostView(postId, payload = {}) {
  return service.post(`/api/posts/${ensureId("postId", postId)}/view`, payload);
}
