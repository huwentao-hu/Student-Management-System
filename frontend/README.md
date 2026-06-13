# 学生管理系统前端

React、TypeScript、Vite 单页应用。当前已实现登录、角色导航、工作台和学生列表。

## 本地运行

```powershell
npm install
Copy-Item .env.example .env.local
npm run dev
```

默认连接 `http://localhost:8080`。可在 `.env.local` 中设置：

```text
VITE_API_BASE_URL=https://你的后端域名
```

## 检查与构建

```powershell
npm run lint
npm run build
```

生产构建输出到 `dist`，可直接部署到 Cloudflare Pages。
