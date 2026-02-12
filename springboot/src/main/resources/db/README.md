# Academic Early Warning System 数据库说明

## 1. 脚本说明
- `schema.sql`：建表脚本（用户、课程、任务、行为、考试、风险记录等）。
- `seed_data.sql`：联调用测试数据（含管理员/教师/学生账号、任务、行为、风险数据）。

## 2. 初始化顺序（MySQL 5.7）
```sql
source /workspace/AcademicEarlyWarningSystem/springboot/src/main/resources/db/schema.sql;
source /workspace/AcademicEarlyWarningSystem/springboot/src/main/resources/db/seed_data.sql;
```

## 3. 登录联调账号（统一密码：`123456`）
- 管理员：`admin01`（`ADMIN`）
- 教师：`teacher01`（`TEACHER`）
- 学生：`stu1001` / `stu1002` / `stu1003`（`STUDENT`）

## 4. 登录接口测试示例
```bash
curl -X POST 'http://localhost:9090/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin01","password":"123456","role":"ADMIN"}'
```

```bash
curl -X POST 'http://localhost:9090/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"teacher01","password":"123456","role":"TEACHER"}'
```

```bash
curl -X POST 'http://localhost:9090/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"stu1001","password":"123456","role":"STUDENT"}'
```

> 注意：登录时 `role` 必须与账号角色一致，否则会返回“角色不匹配”。
