# 账号角色与登录设计

状态：已实现  
最后更新：2026-06-12

## 目标

建立管理员、教师、学生账号基础，并允许用户通过用户名和密码登录获得会话令牌。

## 角色规则

| 角色 | 当前规则 |
| --- | --- |
| `ADMIN` | 管理所有数据 |
| `TEACHER` | 查询学生，后续管理成绩与考勤 |
| `STUDENT` | 登录后仅查看自己的信息 |

每个账号只有一个角色。`STUDENT` 账号必须关联一条 `students` 记录，其他角色不关联学生记录。

## 数据表

### `user_accounts`

- 用户名全局唯一，首版登录区分大小写。
- 密码使用 BCrypt 哈希保存。
- 保存角色、启用状态和可选的学生关联。

### `auth_tokens`

- 登录后生成 32 字节安全随机令牌。
- 原始令牌仅返回给客户端。
- 数据库保存 SHA-256 令牌哈希、所属账号和过期时间。
- 默认有效期 8 小时，可通过 `AUTH_TOKEN_TTL_HOURS` 配置。

## 登录 API

`POST /api/auth/login`

请求：

```json
{
  "username": "admin",
  "password": "your-password"
}
```

成功响应：

```json
{
  "token": "raw-session-token",
  "expiresAt": "2026-06-13T00:00:00Z",
  "userId": 1,
  "username": "admin",
  "role": "ADMIN",
  "studentId": null
}
```

用户名、密码错误或账号禁用均返回相同的 `401 Unauthorized`，避免泄露账号是否存在。

## 初始化管理员

应用启动时，仅当设置 `BOOTSTRAP_ADMIN_PASSWORD` 且管理员用户名尚不存在时，自动创建管理员。用户名默认 `admin`，可通过 `BOOTSTRAP_ADMIN_USERNAME` 修改。

## 业务接口鉴权

客户端通过 `Authorization: Bearer <token>` 访问受保护接口。令牌不存在、无效、过期或账号被禁用时返回 `401 Unauthorized`。

当前学生接口权限：

| 操作 | 管理员 | 教师 | 学生 |
| --- | --- | --- | --- |
| 新增学生 | 允许 | 拒绝 | 拒绝 |
| 查询学生列表 | 允许 | 允许 | 拒绝 |
| 查询单个学生 | 允许 | 允许 | 仅本人 |

## 账号创建

管理员可通过 `POST /api/accounts` 创建教师账号，或为已有学生创建学生账号。该接口不允许创建管理员账号。

教师账号示例：

```json
{
  "username": "teacher01",
  "password": "strong-password",
  "role": "TEACHER"
}
```

学生账号示例：

```json
{
  "username": "student01",
  "password": "strong-password",
  "role": "STUDENT",
  "studentId": 1
}
```

用户名必须唯一；每名学生只能绑定一个学生账号。
