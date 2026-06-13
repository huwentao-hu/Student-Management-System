# AWS EC2 + RDS 公网部署操作手册

最后更新：2026-06-13

本手册用于部署当前学生管理系统。请始终让 EC2、RDS、ECR 和 Systems Manager 使用同一个 AWS Region。示例资源名称可以直接采用：

```text
Region: ap-southeast-1
EC2: student-management-api
ECR: student-management-backend
RDS: student-management-db
EC2 IAM role: StudentManagementEc2Role
GitHub IAM role: StudentManagementGitHubDeployRole
Container: student-management-backend
```

如果主要访问者位于中国大陆，可在 AWS 控制台测试新加坡 `ap-southeast-1`、东京 `ap-northeast-1` 等区域的访问质量后选择。区域一旦确定，后续资源保持一致。

## 1. 创建网络安全组

进入 `EC2 → Network & Security → Security Groups`。

创建 `student-management-ec2-sg`：

- VPC：选择默认 VPC，或你为本项目创建的 VPC。
- 入站规则：HTTP `80` 来源 `0.0.0.0/0`；HTTPS `443` 来源 `0.0.0.0/0`。
- 出站规则：保留允许全部出站。
- 不开放 `5432`、`8000`、`8080`。

创建 `student-management-rds-sg`：

- 与 EC2 选择同一个 VPC。
- 入站规则：PostgreSQL `5432`，来源选择安全组 `student-management-ec2-sg`。
- 不允许 `0.0.0.0/0`。

## 2. 创建私有 RDS PostgreSQL

进入 `RDS → Databases → Create database`：

- Creation method：Standard create
- Engine：PostgreSQL
- Engine version：PostgreSQL 17 的可用版本
- Template：Free tier（账号符合条件时）或 Dev/Test
- DB instance identifier：`student-management-db`
- Master username：`student_admin`
- Master password：生成并妥善保存强密码
- Instance class：按预算选择，演示环境可选择最低可用规格
- Storage：General Purpose SSD，开启 storage autoscaling
- VPC：与 EC2 相同
- Public access：`No`
- VPC security group：移除默认组，选择 `student-management-rds-sg`
- Initial database name：`student_management`
- Backup retention：至少 `7 days`
- Deletion protection：建议开启

创建完成后，在数据库详情的 `Connectivity & security` 中记录 Endpoint。不要记录或发送数据库密码到聊天或仓库。

## 3. 创建 EC2 IAM 角色

进入 `IAM → Roles → Create role`：

- Trusted entity type：AWS service
- Use case：EC2
- 添加托管策略：
  - `AmazonSSMManagedInstanceCore`
  - `AmazonEC2ContainerRegistryReadOnly`
- Role name：`StudentManagementEc2Role`

## 4. 创建 EC2 与 Elastic IP

进入 `EC2 → Instances → Launch instances`：

- Name：`student-management-api`
- AMI：Ubuntu Server 24.04 LTS
- Instance type：按预算选择；演示环境可从 `t3.micro` 起步
- Key pair：可创建用于紧急恢复，但不要长期开放 SSH
- Network settings：与 RDS 相同的 VPC；选择 `student-management-ec2-sg`
- Advanced details → IAM instance profile：`StudentManagementEc2Role`
- Storage：建议至少 16 GiB gp3

创建后进入 `EC2 → Elastic IP addresses → Allocate Elastic IP address`，再执行 `Actions → Associate Elastic IP address`，关联到该实例。记录：

```text
AWS Region
AWS Account ID
EC2 Instance ID
Elastic IP
RDS Endpoint
```

## 5. 通过 Session Manager 初始化 EC2

等待几分钟，然后进入：

```text
EC2 → Instances → student-management-api → Connect → Session Manager → Connect
```

如果 Session Manager 尚不可用，确认实例已附加 `StudentManagementEc2Role`、实例可以访问互联网，并等待 SSM Agent 注册。

执行：

```bash
sudo apt update
sudo apt install -y docker.io awscli nginx certbot python3-certbot-nginx curl
sudo systemctl enable --now docker nginx
sudo mkdir -p /etc/student-management
sudo touch /etc/student-management/backend.env
sudo chmod 600 /etc/student-management/backend.env
sudo nano /etc/student-management/backend.env
```

在编辑器中填写，替换所有占位符：

```dotenv
DB_URL=jdbc:postgresql://RDS_ENDPOINT:5432/student_management
DB_USERNAME=student_admin
DB_PASSWORD=STRONG_DATABASE_PASSWORD
BOOTSTRAP_ADMIN_USERNAME=admin
BOOTSTRAP_ADMIN_PASSWORD=STRONG_ADMIN_PASSWORD
CORS_ALLOWED_ORIGINS=https://YOUR_PROJECT.pages.dev
AUTH_TOKEN_TTL_HOURS=8
```

保存后验证文件存在，但不要把内容打印到终端日志：

```bash
sudo test -s /etc/student-management/backend.env
sudo stat -c '%a %U %G %n' /etc/student-management/backend.env
```

权限结果应为 `600 root root`。

## 6. 创建 ECR

进入 `Elastic Container Registry → Private registry → Repositories → Create repository`：

- Repository name：`student-management-backend`
- Image tag mutability：Mutable
- Encryption：AES-256
- Scan on push：开启

建议添加生命周期规则，仅保留最近约 20 个非 `latest` 镜像，避免长期产生存储费用。

## 7. 创建 GitHub Actions OIDC 身份

进入 `IAM → Identity providers → Add provider`：

- Provider type：OpenID Connect
- Provider URL：`https://token.actions.githubusercontent.com`
- Audience：`sts.amazonaws.com`

如果该 Provider 已存在，不要重复创建。

进入 `IAM → Roles → Create role`：

- Trusted entity type：Web identity
- Identity provider：`token.actions.githubusercontent.com`
- Audience：`sts.amazonaws.com`
- GitHub organization：`huwentao-hu`
- GitHub repository：`Student-Management-System`
- GitHub branch：`main`
- Role name：`StudentManagementGitHubDeployRole`

给该角色添加一条内联权限策略。将 `REGION`、`ACCOUNT_ID`、`INSTANCE_ID` 替换为实际值：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcrAuthorization",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "PushBackendImage",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:CompleteLayerUpload",
        "ecr:GetDownloadUrlForLayer",
        "ecr:InitiateLayerUpload",
        "ecr:PutImage",
        "ecr:UploadLayerPart"
      ],
      "Resource": "arn:aws:ecr:REGION:ACCOUNT_ID:repository/student-management-backend"
    },
    {
      "Sid": "DeployThroughSsm",
      "Effect": "Allow",
      "Action": "ssm:SendCommand",
      "Resource": [
        "arn:aws:ec2:REGION:ACCOUNT_ID:instance/INSTANCE_ID",
        "arn:aws:ssm:REGION::document/AWS-RunShellScript"
      ]
    },
    {
      "Sid": "ReadSsmCommandResult",
      "Effect": "Allow",
      "Action": [
        "ssm:GetCommandInvocation",
        "ssm:ListCommandInvocations"
      ],
      "Resource": "*"
    }
  ]
}
```

保存后记录该角色的 ARN，例如：

```text
arn:aws:iam::123456789012:role/StudentManagementGitHubDeployRole
```

## 8. 配置 GitHub 仓库变量

进入 GitHub 仓库：

```text
Settings → Secrets and variables → Actions
```

在 `Variables` 中创建：

```text
AWS_REGION=实际区域
AWS_ACCOUNT_ID=12位账号ID
ECR_REPOSITORY=student-management-backend
EC2_INSTANCE_ID=实例ID
CONTAINER_NAME=student-management-backend
```

在 `Secrets` 中创建：

```text
AWS_ROLE_ARN=GitHub部署角色ARN
```

代码推送到 `main` 后，`Actions → Deploy backend to EC2` 会自动运行。首次执行成功后，在 EC2 Session Manager 中验证：

```bash
sudo docker ps
curl http://127.0.0.1:8000/api/health
```

## 9. 配置 API 域名和 Nginx

推荐使用独立子域名，例如 `api.your-domain.com`。在 Cloudflare DNS 中添加：

```text
Type: A
Name: api
IPv4 address: EC2 Elastic IP
Proxy status: DNS only
```

等待 DNS 生效后，在 EC2 Session Manager 中执行：

```bash
sudo nano /etc/nginx/sites-available/student-management-api
```

填写并替换域名：

```nginx
server {
    listen 80;
    server_name api.your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/student-management-api /etc/nginx/sites-enabled/student-management-api
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
curl http://api.your-domain.com/api/health
```

## 10. 开启 HTTPS

确保 DNS 已解析到 Elastic IP，然后执行：

```bash
sudo certbot --nginx -d api.your-domain.com
sudo certbot renew --dry-run
curl https://api.your-domain.com/api/health
```

Certbot 会询问邮箱、服务条款和是否重定向 HTTPS。请选择将 HTTP 重定向到 HTTPS。

## 11. 连接 Cloudflare Pages

先修改 EC2 的 `/etc/student-management/backend.env`：

```dotenv
CORS_ALLOWED_ORIGINS=https://YOUR_PROJECT.pages.dev,https://YOUR_FRONTEND_DOMAIN
```

然后在 GitHub Actions 手动运行一次 `Deploy backend to EC2`，让容器读取新环境变量。

进入 Cloudflare Pages 项目：

```text
Settings → Environment variables
```

添加 Production 环境变量：

```text
VITE_API_BASE_URL=https://api.your-domain.com
```

重新部署前端，并验证登录、学生列表与课程列表。

## 12. 故障检查

EC2：

```bash
sudo docker ps
sudo docker logs --tail 200 student-management-backend
curl http://127.0.0.1:8000/api/health
sudo nginx -t
sudo systemctl status nginx --no-pager
```

RDS 无法连接时，重点检查：

- RDS 与 EC2 是否位于同一 VPC。
- RDS 是否为 `Public access: No`。
- RDS 安全组 `5432` 的来源是否为 EC2 安全组。
- `DB_URL` 是否使用 RDS Endpoint 和数据库名 `student_management`。

GitHub Actions 失败时，重点检查：

- OIDC 角色信任策略是否限制到正确仓库和 `main` 分支。
- GitHub Variables 与 `AWS_ROLE_ARN` 是否完整。
- EC2 是否出现在 `Systems Manager → Fleet Manager → Managed nodes`。
- EC2 IAM 角色是否同时包含 SSM 与 ECR 只读策略。

## 13. 成本与数据安全

- AWS 资源通常会产生费用，创建前查看区域定价和 Free Tier 条件。
- 为 RDS 开启自动备份，并定期测试恢复。
- 不公开 RDS，不把密码提交到 GitHub。
- 不再需要环境时，先创建 RDS 最终快照，再删除 EC2、Elastic IP、RDS 与无用 ECR 镜像。
