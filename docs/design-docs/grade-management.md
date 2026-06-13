# 成绩管理设计

状态：已实现  
最后更新：2026-06-12

## 目标

为学生在具体开课安排中的学习结果记录百分制成绩。

## 数据模型

`grades` 表包含：

| 字段 | 规则 |
| --- | --- |
| `studentId` | 必填，关联学生 |
| `courseOfferingId` | 必填，关联开课安排 |
| `score` | 必填，范围 0.0 至 100.0，最多一位小数 |

同一学生对同一开课安排最多一条成绩。重复录入使用更新接口覆盖分数，不新增历史版本。

## 业务规则

- 学生在开课安排所属学期内必须归属过该开课班级，才能登记成绩。
- 管理员可录入、修改和查询全部成绩。
- 授课教师只能录入、修改和查询自己负责开课安排的成绩。
- 学生只能查询自己的成绩。
- 同一学生的成绩写入前锁定学生记录，并发首次写入串行执行，最终只保留一条成绩。

## API

### 录入或修改成绩

`PUT /api/grades`

```json
{
  "studentId": 1,
  "courseOfferingId": 1,
  "score": 88.5
}
```

首次录入返回 `201 Created`，已有成绩更新返回 `200 OK`。

### 查询成绩

- `GET /api/grades/{id}`
- `GET /api/grades?studentId=1&courseOfferingId=1&academicYear=2026&semester=FIRST&page=0&size=20`

成绩统计接口及口径见 `grade-statistics.md`。

## 错误响应

- 学生、开课安排或成绩不存在：`404 Not Found`
- 学生未修读该开课安排或分数无效：`400 Bad Request`
- 越权录入或查询：`403 Forbidden`
