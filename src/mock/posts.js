/**
 * 文章 mock 数据中心：
 * - 列表页使用 summaries
 * - 详情页使用 details
 * 这样可以保证“列表可见”与“详情可读”使用同一套数据，不会出现断链。
 */

export const MOCK_POST_SUMMARIES = [
  {
    id: 2601,
    title: "基利曼战术手记：零 localStorage 登录链路落地实录",
    summary:
      "以 HttpOnly Cookie + Pinia 内存态实现会话闭环，覆盖登录、静默续期、越权拦截与安全回退。",
    tags: ["Auth", "RBAC", "Pinia", "Cookie"],
    like_count: 88,
    view_count: 2337,
    cover_url: "/images/auth-bg-guilliman-throne.jpg",
    updated_at: "2026-04-08T19:46:00+08:00",
    is_pinned: 1
  },
  {
    id: 201,
    title: "Nginx 反向代理与静态资源分离",
    summary: "整理个人云服务器部署流程与常见踩坑修复路径。",
    tags: ["Nginx", "部署"],
    like_count: 23,
    view_count: 877,
    cover_url: "/images/ultramarines-squad.png",
    updated_at: "2026-04-05T20:16:00+08:00"
  },
  {
    id: 202,
    title: "MySQL 表结构设计复盘",
    summary: "围绕文章、评论、通知、会话四类核心表给出索引策略。",
    tags: ["MySQL", "Schema"],
    like_count: 32,
    view_count: 1041,
    cover_url: "/images/imperial-eagle.webp",
    updated_at: "2026-04-06T10:03:00+08:00"
  },
  {
    id: 203,
    title: "SSE 与轮询降级事件流",
    summary: "处理浏览器断流、重连与补偿拉取的一体化策略。",
    tags: ["SSE", "Resilience"],
    like_count: 16,
    view_count: 698,
    cover_url: "/images/ultra-helmet-alt.png",
    updated_at: "2026-04-06T14:40:00+08:00"
  }
];

const MOCK_POST_DETAILS = {
  2601: {
    id: 2601,
    title: "基利曼战术手记：零 localStorage 登录链路落地实录",
    content_html: `
      <h2 id="context">战区背景</h2>
      <p>本项目要求认证态不能落地到 localStorage。我们采用 <code>HttpOnly Cookie</code> 承载凭证，前端只维护 Pinia 内存态，降低 XSS 窃取会话风险。</p>

      <h2 id="objective">架构目标</h2>
      <p>目标不是“能登录”而已，而是形成完整会话闭环：登录、刷新、登出、受保护路由拦截、管理员权限校验全部同一策略管理。</p>

      <h2 id="flow">核心流程</h2>
      <h3 id="login">1. 登录</h3>
      <p>用户提交用户名与密码后，后端设置 HttpOnly Cookie；前端随后调用 <code>/api/auth/me</code> 拉取当前用户信息并注入 Pinia。</p>

      <h3 id="refresh">2. 静默续期</h3>
      <p>页面刷新或首次进入路由前，执行 <code>/api/auth/refresh</code>。成功后继续拉取 <code>/api/auth/me</code>，失败则回退为游客态。</p>

      <h3 id="guard">3. 路由守卫</h3>
      <p>所有受保护页面统一依赖 <code>meta.requiresAuth</code> 与 <code>meta.requiresAdmin</code>，由全局守卫集中处理，避免组件内重复判断。</p>

      <h2 id="snippet">关键实现片段</h2>
      <pre><code>router.beforeEach(async (to) => {
  await authStore.restoreSession();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "auth", query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: "home" };
  }
  return true;
});</code></pre>

      <h2 id="acceptance">验收要点</h2>
      <p>1) 刷新后登录态可恢复；2) 游客访问详情会被重定向登录；3) 普通用户无法进入后台；4) 退出后受保护路由全部失效。</p>
    `,
    toc_json: [
      { id: "context", text: "战区背景", level: 2 },
      { id: "objective", text: "架构目标", level: 2 },
      { id: "flow", text: "核心流程", level: 2 },
      { id: "login", text: "1. 登录", level: 3 },
      { id: "refresh", text: "2. 静默续期", level: 3 },
      { id: "guard", text: "3. 路由守卫", level: 3 },
      { id: "snippet", text: "关键实现片段", level: 2 },
      { id: "acceptance", text: "验收要点", level: 2 }
    ],
    like_count: 88,
    liked: false,
    updated_at: "2026-04-08T19:46:00+08:00",
    cover_url: "/images/auth-bg-guilliman-throne.jpg"
  }
};

const DEFAULT_DETAIL_ID = 2601;

function cloneDetail(detail) {
  if (!detail || typeof detail !== "object") return null;
  return {
    ...detail,
    toc_json: Array.isArray(detail.toc_json)
      ? detail.toc_json.map((item) => ({ ...item }))
      : []
  };
}

export function getMockPostDetailById(postId) {
  const normalizedId = Number(postId);
  const detail = MOCK_POST_DETAILS[normalizedId] || MOCK_POST_DETAILS[DEFAULT_DETAIL_ID];
  return cloneDetail(detail);
}

