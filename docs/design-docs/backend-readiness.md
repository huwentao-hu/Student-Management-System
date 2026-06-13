# 后端交付准备设计

状态：已实现  
最后更新：2026-06-12

## 目标

补齐前端联调和基本运行检查所需的健康检查、主动退出和跨域访问能力。

## 健康检查

- `GET /api/health` 无需登录。
- 接口执行数据库连通性检查。
- 正常时返回服务状态、数据库状态和检查时间。
- 数据库不可用时返回 `503 Service Unavailable` 和 `DOWN` 状态。

## 主动退出

- `POST /api/auth/logout` 需要有效 Bearer Token。
- 退出后立即删除当前登录令牌，不影响同一账号的其他登录令牌。
- 成功返回 `204 No Content`。
- 被撤销令牌再次访问受保护接口时返回 `401 Unauthorized`。

## 过期令牌清理

- 系统默认每小时自动删除过期登录令牌。
- 管理员可调用 `POST /api/auth/tokens/cleanup` 立即清理，并获得删除数量。
- 清理任务周期和首次延迟可通过环境变量配置。

## CORS

- 默认允许 `http://localhost:5173` 和 `http://localhost:3000`。
- 可通过 `CORS_ALLOWED_ORIGINS` 使用逗号分隔覆盖允许来源。
- 允许 `GET`、`POST`、`PUT`、`DELETE` 和 `OPTIONS`。
- 允许前端发送 `Authorization` 和 `Content-Type` 请求头。
- 浏览器预检请求无需 Bearer Token。
