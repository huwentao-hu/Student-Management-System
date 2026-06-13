# 学生管理系统

当前已完成核心后端与数据库功能，并开始前端开发。前端已具备登录、角色导航、工作台，以及学生列表、新增、详情和编辑流程。

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`，默认连接 `http://localhost:8080`。生产 API 地址通过 `VITE_API_BASE_URL` 配置。

前端生产构建：

```powershell
cd frontend
npm run lint
npm run build
```

构建产物位于 `frontend/dist`，已配置为可部署到 Cloudflare Pages。

GitHub 仓库：`https://github.com/huwentao-hu/Student-Management-System`

## 启动数据库

```powershell
docker compose up -d postgres
```

## 运行测试

```powershell
cd backend
.\mvnw.cmd test
```

## 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认地址：`http://localhost:8080`

公共健康检查：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health"
```

首次创建本地管理员：

```powershell
$env:BOOTSTRAP_ADMIN_PASSWORD = "请设置一个强密码"
.\mvnw.cmd spring-boot:run
```

## 示例请求

登录：

```powershell
$loginBody = @{
  username = "admin"
  password = "请设置一个强密码"
} | ConvertTo-Json

$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method Post -ContentType "application/json" -Body $loginBody

$headers = @{ Authorization = "Bearer $($login.token)" }
```

主动退出当前登录：

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/logout" `
  -Method Post -Headers $headers
```

管理员手动清理过期登录令牌：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/tokens/cleanup" `
  -Method Post -Headers $headers
```

系统默认每小时自动清理过期令牌，可通过以下环境变量调整：

```powershell
$env:AUTH_TOKEN_CLEANUP_INTERVAL_MS = "3600000"
$env:AUTH_TOKEN_CLEANUP_INITIAL_DELAY_MS = "3600000"
```

默认允许 `http://localhost:5173` 和 `http://localhost:3000` 跨域访问。可在启动后端前覆盖：

```powershell
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,https://your-frontend.example"
```

新增学生：

```powershell
$body = @{
  name = "张三"
  enrollmentDate = "2026-09-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/students" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $body
```

学号由系统自动生成，例如 `202600000123`。

修改学生：

```powershell
$updateBody = @{
  name = "张三"
  gender = "MALE"
  enrollmentDate = "2026-09-01"
  status = "ACTIVE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/students/1" `
  -Method Put -ContentType "application/json" -Headers $headers -Body $updateBody
```

分页搜索：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/students?keyword=张&status=ACTIVE&page=0&size=20" `
  -Headers $headers
```

创建教师账号：

```powershell
$accountBody = @{
  username = "teacher01"
  password = "请设置一个强密码"
  role = "TEACHER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/accounts" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $accountBody
```

创建班级：

```powershell
$classBody = @{
  name = "软件工程1班"
  entryYear = 2026
  homeroomTeacherId = 2
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/classes" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $classBody
```

学生入班或转班：

```powershell
$assignmentBody = @{
  classId = 1
  effectiveDate = "2026-09-01"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/students/1/class-assignments" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $assignmentBody
```

查询学生分班历史：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/students/1/class-assignments" -Headers $headers
```

创建课程：

```powershell
$courseBody = @{
  name = "数据库原理"
  credits = 3.5
  status = "ACTIVE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/courses" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $courseBody
```

课程编号由系统自动生成，例如 `C00000001`。管理员和教师可通过 `/api/courses` 查询课程目录。

创建开课安排：

```powershell
$offeringBody = @{
  courseId = 1
  classId = 1
  teacherId = 2
  academicYear = 2026
  semester = "FIRST"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/course-offerings" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $offeringBody
```

查询开课安排：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/course-offerings?classId=1&academicYear=2026&semester=FIRST" `
  -Headers $headers
```

查询学生课程表：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/students/1/timetable?academicYear=2026&semester=FIRST" `
  -Headers $headers
```

学生账号只能查询自己的课程表；管理员和教师可以代查。课程表根据该学期内的分班历史与班级开课安排实时计算。

录入或修改成绩：

```powershell
$gradeBody = @{
  studentId = 1
  courseOfferingId = 1
  score = 88.5
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/grades" `
  -Method Put -ContentType "application/json" -Headers $headers -Body $gradeBody
```

查询成绩：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/grades?academicYear=2026&semester=FIRST" `
  -Headers $headers
```

管理员可管理全部成绩，授课教师只管理自己负责课程的成绩，学生只能查询本人。
同一学生的并发成绩写入会自动串行处理，最终只保留一条成绩记录。

查询教学班成绩统计：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/grade-statistics/course-offerings/1" `
  -Headers $headers
```

查询学生学期成绩汇总：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/grade-statistics/students/1?academicYear=2026&semester=FIRST" `
  -Headers $headers
```

教学班统计包含平均分、最高分、最低分和及格率；学生学期汇总包含总学分和学分加权平均分。
成绩统计由数据库直接执行聚合查询。

创建课堂考勤场次：

```powershell
$sessionBody = @{
  courseOfferingId = 1
  sessionDate = "2026-10-01"
  topic = "数据库关系模型"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/attendance-sessions" `
  -Method Post -ContentType "application/json" -Headers $headers -Body $sessionBody
```

登记或修改学生考勤：

```powershell
$attendanceBody = @{
  attendanceSessionId = 1
  studentId = 1
  status = "PRESENT"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/attendance-records" `
  -Method Put -ContentType "application/json" -Headers $headers -Body $attendanceBody
```

学生可通过 `/api/attendance-records` 查询本人考勤。
同一学生同一课堂的并发考勤写入会自动串行处理，最终只保留一条考勤记录。

查询教学班考勤统计：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/attendance-statistics/course-offerings/1" `
  -Headers $headers
```

查询学生学期考勤汇总：

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/attendance-statistics/students/1?academicYear=2026&semester=FIRST" `
  -Headers $headers
```

考勤统计以已登记记录为分母，出勤和迟到计入出勤率；未登记学生不会自动视为缺勤。
考勤状态数量由数据库直接执行聚合查询。

项目计划见 `docs/plans.md`，后端规范见 `docs/design.md`。
