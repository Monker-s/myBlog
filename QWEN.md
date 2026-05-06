# 个人知识库前端 (Personal Knowledge Frontend)

## 项目概述

这是一个基于 **Vue 3 + Vite + Element Plus + Pinia + Vue Router** 构建的个人知识库前端应用。项目采用现代化的前端技术栈，提供文章浏览、管理、AI 对话、通知系统以及后台管理等功能。

### 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios / Fetch API
- **语言**: JavaScript (ES Module)

### 项目架构

```
src/
├── api/              # API 请求模块（http.js 封装 Fetch，各业务模块接口）
├── assets/           # 静态资源（图片、字体等）
├── components/       # 可复用组件
├── composables/      # 组合式函数（Composition API 逻辑复用）
├── constants/        # 常量定义（如路由名称）
├── directives/       # 自定义指令
├── layouts/          # 页面布局组件（AppLayout、AdminLayout）
├── mock/             # Mock 数据
├── router/           # 路由配置（含导航守卫）
├── stores/           # Pinia 状态管理（app、auth、notifications）
├── styles/           # 全局样式（重置、变量、主题、过渡动画）
├── utils/            # 工具函数（日期、消毒、SEO、服务拦截器）
├── views/            # 页面视图
│   ├── admin/        # 后台管理页面
│   ├── ai/           # AI 对话页面
│   ├── articles/     # 文章列表与详情
│   ├── auth/         # 认证页面
│   └── notifications/ # 通知页面
├── App.vue           # 根组件
└── main.js           # 应用入口
```

## 功能模块

- **首页与文章浏览**: 支持文章列表展示与详情查看
- **用户认证**: 登录/注册功能，支持会话恢复
- **通知系统**: 用户通知管理
- **AI 对话**: 内置 AI 聊天功能
- **后台管理**: 文章管理、用户管理、装饰管理、UV 数据看板
- **路由守卫**: 自动鉴权，未登录跳转登录页，管理员权限控制

## 开发指南

### 环境要求

- Node.js 18+
- npm / yarn / pnpm

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

默认访问地址: `http://localhost:5173`

### 构建生产版本

```bash
npm run build
```

构建产物输出至 `dist/` 目录。

### 预览生产构建

```bash
npm run preview
```

### 环境变量

项目使用 `.env` 文件管理环境变量，参考 `.env.example`:

| 变量名              | 说明          | 示例值                  |
| ------------------- | ------------- | ----------------------- |
| `VITE_API_BASE_URL` | 后端 API 地址 | `http://localhost:3000` |
| `VITE_APP_TITLE`    | 应用标题      | `个人知识库`            |

## 代码规范

- 使用 `@/` 别名指向 `src/` 目录（配置于 `vite.config.js` 和 `jsconfig.json`）
- API 请求统一通过 `src/api/http.js` 封装的 `request` 方法，支持请求/响应拦截
- 路由名称集中定义在 `src/constants/routes.js`，避免硬编码
- 组件采用 Vue 3 Composition API 风格
- 状态管理使用 Pinia，按模块划分 store

## 测试

测试目录结构:

```
tests/
├── e2e/    # 端到端测试
└── unit/   # 单元测试
```

目前测试目录为空，可根据需要添加 Vitest 或 Playwright 等测试框架。

## 相关文档

- [Vue 3 文档](https://vuejs.org/)
- [Vite 文档](https://vitejs.dev/)
- [Element Plus 文档](https://element-plus.org/)
- [Pinia 文档](https://pinia.vuejs.org/)
- [Vue Router 文档](https://router.vuejs.org/)
