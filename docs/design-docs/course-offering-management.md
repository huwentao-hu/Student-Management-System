# 开课安排管理设计

状态：已实现  
最后更新：2026-06-12

## 目标

将课程目录中的课程安排给具体班级和授课教师，并记录所属学年与学期，为后续成绩和考勤提供引用对象。

## 数据模型

`course_offerings` 表包含：

| 字段 | 规则 |
| --- | --- |
| `courseId` | 必填，必须关联状态为 `ACTIVE` 的课程 |
| `classId` | 必填，关联一个班级 |
| `teacherId` | 必填，必须关联已启用的教师账号 |
| `academicYear` | 必填，表示学年起始年份，范围 1900 至 2200 |
| `semester` | 必填，`FIRST` 或 `SECOND` |

同一班级、同一学年学期不能重复安排同一课程。同一课程可以安排给不同班级，同一教师可以承担多条开课安排。

## API

### 创建开课安排

`POST /api/course-offerings`

仅管理员可调用。

```json
{
  "courseId": 1,
  "classId": 1,
  "teacherId": 2,
  "academicYear": 2026,
  "semester": "FIRST"
}
```

### 查询开课安排

- `GET /api/course-offerings/{id}`
- `GET /api/course-offerings?courseId=1&classId=1&teacherId=2&academicYear=2026&semester=FIRST&page=0&size=20`

管理员和教师可查询开课安排管理接口。学生通过独立课程表接口，结合指定学期内的分班历史查看本人课程。

成绩记录通过 `courseOfferingId` 关联具体开课安排。

课堂考勤场次也通过 `courseOfferingId` 关联具体开课安排。

## 错误响应

- 开课安排、课程或班级不存在：`404 Not Found`
- 同一班级同一学年学期重复安排课程：`409 Conflict`
- 课程未启用、教师无效或参数不符合规则：`400 Bad Request`
- 非管理员创建或学生访问：`403 Forbidden`
