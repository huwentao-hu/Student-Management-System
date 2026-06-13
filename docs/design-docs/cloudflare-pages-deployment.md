# Cloudflare Pages 部署设计

最后更新：2026-06-13

Cloudflare Pages 只托管 `frontend` 构建出的静态文件。Spring Boot 后端部署到 EC2 Docker 并由 Nginx 提供 HTTPS API；PostgreSQL 使用禁止公网访问的 RDS。

## Pages 构建配置

- GitHub 仓库：`https://github.com/huwentao-hu/Student-Management-System`
- Production branch：`main`
- Root directory：`frontend`
- Build command：`npm run build`
- Build output directory：`dist`
- 环境变量：`VITE_API_BASE_URL=https://api.751905.xyz`

`frontend/public/_redirects` 中的 `/* /index.html 200` 会进入构建产物，保证直接访问前端子路由时仍返回应用入口。

后端生产环境的 `CORS_ALLOWED_ORIGINS` 必须包含 Cloudflare Pages 的 `pages.dev` 域名和最终自定义域名。前后端均应使用 HTTPS。

## 当前状态

后端已通过 GitHub Actions 自动部署到 EC2，生产 API 为 `https://api.751905.xyz`。HTTPS 健康检查返回服务与数据库均为 `UP`，并已允许 `https://stu.751905.xyz` 和 `https://student-management-system-2q4.pages.dev` 跨域访问。

最后一步是在 Cloudflare Pages Production 环境配置 `VITE_API_BASE_URL=https://api.751905.xyz` 并重新部署前端。
