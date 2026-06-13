# 学生新增与查询设计

状态：已实现  
最后更新：2026-06-12

## 目标

允许管理员或未来的管理端前端新增学生档案，并按数据库 ID 或学号查询单个学生。

## 学生字段

| 字段 | 必填 | 规则 |
| --- | --- | --- |
| `studentNumber` | 系统生成 | 入学年份 + 8 位数据库主键序号，创建后不可修改 |
| `name` | 是 | 最长 100 字符 |
| `gender` | 否 | 暂为最长 16 字符文本，枚举规则待确认 |
| `dateOfBirth` | 否 | 必须早于当前日期 |
| `phone` | 否 | 最长 32 字符 |
| `email` | 否 | 必须为合法邮箱格式 |
| `enrollmentDate` | 否 | 入学日期 |
| `status` | 系统赋值 | 新增时默认为 `ACTIVE` |

## 数据表

`students` 表由 `V1__create_students.sql` 创建。状态可取：

- `ACTIVE`
- `SUSPENDED`
- `GRADUATED`
- `WITHDRAWN`

## API

### 新增学生

`POST /api/students`

```json
{
  "name": "张三",
  "gender": "MALE",
  "dateOfBirth": "2008-05-12",
  "phone": "13800000000",
  "email": "zhangsan@example.com",
  "enrollmentDate": "2026-09-01"
}
```

### 按 ID 查询

`GET /api/students/{id}`

### 按学号查询

`GET /api/students?studentNumber=20260001`

## 已知待确认项

- 性别字段是否使用固定枚举，以及枚举值。
- 是否要求手机号唯一。
- 哪些学生字段必须填写。

## 学号生成

- 格式：`YYYY########`，例如 `202600000123`。
- `YYYY` 取入学日期年份；未填写入学日期时取创建年份。
- 后八位使用数据库主键序号并补零。
- 数据库主键保证并发新增时学号唯一。
- 修改入学日期不会重算学号。
