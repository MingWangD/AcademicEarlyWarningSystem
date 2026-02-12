-- 关键看板查询 explain，用于观察是否走索引
EXPLAIN SELECT risk_level, COUNT(*)
FROM user
WHERE role='STUDENT'
GROUP BY risk_level;

EXPLAIN SELECT calc_date, AVG(risk_score)
FROM risk_record
GROUP BY calc_date
ORDER BY calc_date DESC
LIMIT 7;

EXPLAIN SELECT student_id, risk_level, risk_score, calc_date
FROM risk_record
ORDER BY id DESC
LIMIT 10;
