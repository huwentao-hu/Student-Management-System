# 考勤管理设计

状态：已实现  
最后更新：2026-06-12

## 目标

记录具体开课安排的课堂场次，以及每名学生在该课堂的考勤状态。

## 数据模型

`attendance_sessions` 表记录课堂场次：

| 字段 | 规则 |
| --- | --- |
| `courseOfferingId` | 必填，关联开课安排 |
| `sessionDate` | 必填，必须处于开课安排所属学期日期范围内 |
| `topic` | 可选，最长 200 字符 |

同一开课安排同一天首版只允许一个课堂场次。

`attendance_records` 表记录学生考勤：

| 字段 | 规则 |
| --- | --- |
| `attendanceSessionId` | 必填，关联课堂场次 |
| `studentId` | 必填，关联学生 |
| `status` | `PRESENT`、`LATE`、`EXCUSED` 或 `ABSENT` |

同一学生对同一课堂场次最多一条考勤记录。

## 业务规则

- 学生在课堂日期当天必须归属开课班级，才能登记考勤。
- 管理员可创建、管理和查询全部考勤。
- 授课教师只能创建、管理和查询自己负责开课安排的考勤。
- 学生只能查询自己的考勤记录。
- 同一学生的考勤写入前锁定学生记录，并发首次写入串行执行，最终只保留一条考勤。

## API

- `POST /api/attendance-sessions`
- `GET /api/attendance-sessions/{id}`
- `GET /api/attendance-sessions?courseOfferingId=1&sessionDate=2026-09-10&page=0&size=20`
- `PUT /api/attendance-records`
- `GET /api/attendance-records/{id}`
- `GET /api/attendance-records?studentId=1&courseOfferingId=1&status=ABSENT&page=0&size=20`

首次登记学生考勤返回 `201 Created`，已有记录修改返回 `200 OK`。

考勤统计接口及口径见 `attendance-statistics.md`。
