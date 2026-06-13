# 执行记录：学生新增与查询

日期：2026-06-12  
状态：已完成

## 完成内容

- 生成 Spring Boot Maven Wrapper 工程。
- 配置 PostgreSQL、JPA、Flyway 和参数校验。
- 创建 `students` 表首个迁移。
- 实现学生新增、按 ID 查询、按学号查询。
- 实现学号冲突和基础参数校验错误响应。
- 添加 Docker Compose PostgreSQL 环境。
- 添加新增、查询、重复学号集成测试。

## 验证结果

- `backend\mvnw.cmd test`：通过，2 个测试，0 失败。
- `docker compose config`：通过。
- PostgreSQL 17 容器：健康运行。
- 真实 PostgreSQL 上 Flyway `V1`：执行成功。
- 真实 PostgreSQL 上调用新增学生接口：成功返回 `201 Created`。

## 实施中修正

- Initializr 元数据提供的版本带 `.RELEASE` 后缀，但 Maven Central 实际版本不带后缀，最终使用 Spring Boot `4.0.7`。
- 中文项目目录导致 Compose 无法推导项目名，已在 `compose.yaml` 显式设置 `name`。
- 测试不再假设数据库自增 ID 从 `1` 开始。
