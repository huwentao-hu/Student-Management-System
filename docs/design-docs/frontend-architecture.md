# 前端架构设计

最后更新：2026-06-13

## 技术方案

- React 19 + TypeScript + Vite。
- React Router 管理单页应用路由，Lucide React 提供统一图标。
- 前端构建为静态站点，计划托管到 Cloudflare Pages。
- Spring Boot API 与 PostgreSQL 独立部署，前端通过 `VITE_API_BASE_URL` 连接后端。

## 当前纵向功能

- 真实用户名密码登录，会话令牌过期后自动清理。
- 根据 `ADMIN`、`TEACHER`、`STUDENT` 角色显示导航。
- 响应式后台应用外壳、工作台与模块入口。
- 管理员和教师可搜索、筛选学生列表并查看学生详情。
- 管理员可新增学生、编辑学生资料和状态；系统生成学号保持只读。
- 管理员和教师可查询班级并查看详情，管理员可新增班级。
- 学生详情支持查看分班历史，管理员可执行入班、转班和离班。
- 课程、课程表、成绩、考勤和设置页面暂为后续接入入口。

## 代码结构

- `frontend/src/App.tsx`：路由、布局、学生列表、表单与详情页面。
- `frontend/src/api.ts`：API 地址、认证请求和会话管理。
- `frontend/src/types.ts`：前后端响应形状的前端类型。
- `frontend/public/_redirects`：Cloudflare Pages 单页应用路由回退。

前端导航按角色隐藏无权页面，但后端权限校验始终是最终安全边界。
