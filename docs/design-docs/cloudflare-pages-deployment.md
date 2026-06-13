# Cloudflare Pages 部署设计

最后更新：2026-06-12

Cloudflare Pages 只托管 `frontend` 构建出的静态文件。Spring Boot 后端和 PostgreSQL 数据库需要部署到独立的公网服务，并提供 HTTPS API 地址。

## Pages 构建配置

- GitHub 仓库：`https://github.com/huwentao-hu/Student-Management-System`
- Production branch：`main`
- Root directory：`frontend`
- Build command：`npm run build`
- Build output directory：`dist`
- 环境变量：`VITE_API_BASE_URL=https://你的后端域名`

`frontend/public/_redirects` 中的 `/* /index.html 200` 会进入构建产物，保证直接访问前端子路由时仍返回应用入口。

后端生产环境的 `CORS_ALLOWED_ORIGINS` 必须包含 Cloudflare Pages 的 `pages.dev` 域名和最终自定义域名。前后端均应使用 HTTPS。

## 当前状态

项目源代码已推送到 GitHub `main` 分支。下一步需要在 Cloudflare Pages 控制台连接仓库并填写上述构建配置。
