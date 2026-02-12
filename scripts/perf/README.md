# 性能测试说明

## 1) API 负载测试（k6）

```bash
k6 run scripts/perf/k6-api-load.js
```

- 测试内容：登录接口 + 教师看板接口
- 默认并发：20 VUs，持续 30 秒
- 目标阈值：95 分位响应时间 < 800ms，错误率 < 5%

## 2) 数据库查询性能检查（MySQL）

先执行：
```sql
source /workspace/AcademicEarlyWarningSystem/springboot/src/main/resources/db/schema.sql;
source /workspace/AcademicEarlyWarningSystem/springboot/src/main/resources/db/seed_data.sql;
```

再执行性能检查 SQL：
```sql
source /workspace/AcademicEarlyWarningSystem/springboot/src/main/resources/db/perf/perf_check.sql;
```

## 3) 数据库并发压测（mysqlslap 示例）

```bash
mysqlslap -uroot -p123456 --concurrency=30 --iterations=10 --query="SELECT risk_level, COUNT(*) FROM user WHERE role='STUDENT' GROUP BY risk_level" code2026
```

```bash
mysqlslap -uroot -p123456 --concurrency=30 --iterations=10 --query="SELECT calc_date, AVG(risk_score) FROM risk_record GROUP BY calc_date ORDER BY calc_date DESC LIMIT 7" code2026
```
