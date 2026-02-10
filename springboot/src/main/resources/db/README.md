# Academic Early Warning System 数据库说明

- `schema.sql` 覆盖用户、课程、任务、作业提交、视频行为、考试、考试提交、日行为汇总和风险记录。
- 表结构与 `/api/v1` 接口参数一一映射，支持角色鉴权、分页查询、预警看板和逻辑回归风险结果落库。
- 建议初始化三类账号（STUDENT/TEACHER/ADMIN）时先使用 BCrypt 加密密码后插入 `user.password` 字段。
