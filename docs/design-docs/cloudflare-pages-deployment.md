# Cloudflare Pages 部署设计

最后更新：2026-06-12

Cloudflare Pages 只托管 `frontend` 构建出的静态文件。Spring Boot 后端部署到 EC2 Docker 并由 Nginx 提供 HTTPS API；PostgreSQL 使用禁止公网访问的 RDS。

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

项目源代码已推送到 GitHub `main` 分支。后端 EC2 自动部署配置已经准备，待完成 AWS 首次部署并取得 HTTPS API 域名后，在 Cloudflare Pages 控制台填写 `VITE_API_BASE_URL`。
