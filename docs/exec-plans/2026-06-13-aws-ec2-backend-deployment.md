# AWS EC2 后端部署执行计划

日期：2026-06-13

## 目标

将 Spring Boot 后端部署到 AWS EC2，将 PostgreSQL 部署到私有 RDS，并建立 GitHub Actions 自动部署、Nginx 反向代理和 HTTPS。

## 验收条件

- 后端 Docker 镜像可以本地构建并通过健康检查。
- RDS 不允许公网访问，且只允许 EC2 安全组连接 `5432`。
- EC2 仅公开 `80/443`，后端容器只绑定 `127.0.0.1:8000`。
- GitHub Actions 能使用 OIDC 推送 ECR 并通过 SSM 部署。
- 公网 HTTPS API 的 `/api/health` 返回服务和数据库正常。
- Cloudflare Pages 使用公网 API 地址并可正常登录。

## 执行步骤

1. 添加后端多阶段 Dockerfile 和忽略文件。
2. 添加 GitHub Actions 自动测试、构建、推送和 SSM 部署工作流。
3. 创建 VPC 网络中的私有 RDS PostgreSQL。
4. 创建 EC2、Elastic IP、EC2 IAM 角色和安全组。
5. 安装 Docker、AWS CLI、Nginx、Certbot，并写入生产环境变量。
6. 创建 ECR、GitHub OIDC Provider 和 GitHub 部署 IAM 角色。
7. 配置 GitHub Variables 与 Secret，运行首次自动部署。
8. 配置 API 域名、Nginx 和 Let's Encrypt HTTPS。
9. 更新 Cloudflare Pages 的 `VITE_API_BASE_URL` 和后端 CORS。
10. 完成公网健康检查、登录及核心接口验证。

## 当前状态

- 已完成项目部署配置文件。
- 后端自动化测试通过，共 44 项。
- 后端生产 Docker 镜像构建通过。
- Docker 容器连接 PostgreSQL 启动成功，`/api/health` 返回服务与数据库均为 `UP`。
- 已创建区域 `ap-southeast-1` 中的私有 RDS、EC2 安全组和 RDS 安全组。
- 已验证 RDS Endpoint 解析到私网地址，且无法从本机公网连接 `5432`。
- 已创建 EC2、Elastic IP 与 EC2 IAM 角色，Session Manager 已成功连接。
- 已配置 GitHub Variables：`AWS_REGION`、`AWS_ACCOUNT_ID`、`ECR_REPOSITORY`、`CONTAINER_NAME`、`EC2_INSTANCE_ID`。
- 已初始化 EC2，并创建 ECR 与 GitHub OIDC 部署角色。
- 首次 GitHub Actions 暴露 Linux Maven Wrapper 执行权限问题，工作流已改为通过 Bash 调用。
- 等待重新运行首次自动部署。

## 已创建 AWS 资源

```text
AWS Account ID: 059535450246
AWS Region: ap-southeast-1
VPC ID: vpc-07a4d37cf359f18cf
EC2 Security Group: sg-0f46592065cf59169
RDS Security Group: sg-095667a2152dda46e
RDS Endpoint: student-management-db.c1mkysoogwzv.ap-southeast-1.rds.amazonaws.com
EC2 Instance ID: i-02f6c7536b1731823
Elastic IP: 3.0.7.232
```
