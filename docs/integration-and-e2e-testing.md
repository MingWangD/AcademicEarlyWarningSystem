# 集成与系统测试清单

## 接口集成测试（后端）

已新增 Spring Boot 集成测试：
- `AuthFlowIntegrationTest`：验证 `/api/v1/auth/login` 返回 JWT 与角色匹配。
- `TeacherRiskIntegrationTest`：验证 `/api/v1/teacher/risk/calc` 返回 `updatedCount` 与 `modelParams`。

运行：
```bash
cd springboot
mvn test -Dtest='*IntegrationTest'
```

## 前端 UI E2E（Cypress）

已新增 Cypress 测试：
- `portal-login.cy.js`：验证登录页角色选择、Mock 登录跳转、路由守卫。

运行：
```bash
cd vue
npm run e2e:run
```

## 联调流程建议

1. 执行数据库脚本：`schema.sql` + `seed_data.sql`
2. 启动后端（9090）与前端（5173）
3. 先跑后端集成测试，再跑 Cypress E2E
4. 使用 `scripts/perf/k6-api-load.js` 做基础压力验证
