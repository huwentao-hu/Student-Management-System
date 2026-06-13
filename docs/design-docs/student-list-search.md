# 学生列表与搜索设计

状态：已实现  
最后更新：2026-06-12

## 目标

为管理端提供学生分页列表，并支持按学号或姓名模糊搜索、按学生状态筛选。

## API

`GET /api/students`

查询参数：

| 参数 | 必填 | 默认值 | 规则 |
| --- | --- | --- | --- |
| `keyword` | 否 | 无 | 不区分大小写，模糊匹配学号或姓名 |
| `status` | 否 | 无 | `ACTIVE`、`SUSPENDED`、`GRADUATED`、`WITHDRAWN` |
| `page` | 否 | `0` | 从 0 开始，不得小于 0 |
| `size` | 否 | `20` | 1 至 100 |

结果固定按学号升序排列。

示例：

`GET /api/students?keyword=张&status=ACTIVE&page=0&size=20`

## 分页响应

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## 兼容现有精确查询

`GET /api/students?studentNumber=20260001` 仍用于按完整学号查询单个学生。带有 `studentNumber` 参数时不会进入分页列表接口。

## 数据库影响

本功能未修改数据库结构，不需要新增 Flyway 迁移。当前数据量较小时直接使用组合查询；数据量增长后再根据查询性能添加索引。
