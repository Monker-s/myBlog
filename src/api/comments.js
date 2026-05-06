import service from "@/utils/service";

function ensureId(name, value) {
  if (value === undefined || value === null || value === "") {
    throw new TypeError(`${name} is required.`);
  }
  return value;
}

export function getPostComments(postId, params = {}) {
  const { pageSize = 20, lastCommentTime } = params;
  const queryParams = { pageSize };
  // 首次加载不传 lastCommentTime，加载更多时才传
  if (lastCommentTime !== undefined && lastCommentTime !== null) {
    queryParams.lastCommentTime = lastCommentTime;
  }
  // 添加时间戳破坏浏览器缓存
  queryParams._t = Date.now();
  return service.get(`/api/posts/${ensureId("postId", postId)}/comments`, {
    params: queryParams,
  });
}

export function createPostComment(postId, payload) {
  return service.post(
    `/api/posts/${ensureId("postId", postId)}/comments`,
    payload,
  );
}

export function replyComment(commentId, payload) {
  return service.post(
    `/api/comments/${ensureId("commentId", commentId)}/reply`,
    payload,
  );
}
