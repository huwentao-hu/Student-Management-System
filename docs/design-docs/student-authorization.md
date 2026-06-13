# 学生接口鉴权与授权设计

状态：已实现  
最后更新：2026-06-12

## 目标

使用登录返回的 Bearer Token 保护学生接口，并按照管理员、教师、学生角色限制访问。

## 鉴权流程

1. 客户端请求 `/api/students/**` 时携带 `Authorization: Bearer <token>`。
2. 服务端计算令牌 SHA-256 哈希。
3. 查询未过期令牌、账号及关联学生。
4. 检查账号是否启用。
5. 将当前用户信息传递给学生接口执行资源权限校验。

## 权限矩阵

| 接口 | 管理员 | 教师 | 学生 |
| --- | --- | --- | --- |
| `POST /api/students` | 允许 | `403` | `403` |
| `GET /api/students` 分页列表 | 允许 | 允许 | `403` |
| `GET /api/students/{id}` | 允许 | 允许 | 仅本人 |
| `GET /api/students?studentNumber=...` | 允许 | 允许 | 仅本人 |

## 错误响应

- 缺少、无效或过期令牌：`401 Unauthorized`
- 已登录但角色或资源权限不足：`403 Forbidden`

## 数据库影响

本功能复用 `auth_tokens` 和 `user_accounts`，未修改数据库结构。
