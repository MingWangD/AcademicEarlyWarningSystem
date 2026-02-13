SET FOREIGN_KEY_CHECKS = 0;

-- 强制清空所有引用 user 的表（你脚本里已经列了顺序，这里保持一致）
TRUNCATE TABLE risk_record;
TRUNCATE TABLE student_daily_activity;
TRUNCATE TABLE exam_submission;
TRUNCATE TABLE exam;
TRUNCATE TABLE video_watch_record;
TRUNCATE TABLE homework_submission;
TRUNCATE TABLE task_question;
TRUNCATE TABLE question_bank;
TRUNCATE TABLE task;
TRUNCATE TABLE course;
TRUNCATE TABLE user;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 用户（密码统一为 123456）
-- BCrypt hash: $2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6
-- 规则：id=1 管理员；id=2 教师；id=3~52 学生 50 人
-- ----------------------------

-- 先插管理员、教师
INSERT INTO user (id, username, password, email, name, role, risk_level, enabled, created_at) VALUES
                                                                                                  (1, 'admin01',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'admin01@school.edu',   '系统管理员', 'ADMIN',   'LOW', 1, NOW()),
                                                                                                  (2, 'teacher01', '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'teacher01@school.edu', '张老师',     'TEACHER', 'LOW', 1, NOW());

-- 再批量插 50 个学生（id=3..52, username=stu1001..stu1050）
INSERT INTO user (id, username, password, email, name, role, risk_level, enabled, created_at)
SELECT
    2 + t.n AS id,
    CONCAT('stu', 1000 + t.n) AS username,
    '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6' AS password,
    CONCAT('stu', 1000 + t.n, '@school.edu') AS email,
    CONCAT(
            CASE MOD(t.n-1, 5)
                WHEN 0 THEN '王同学'
                WHEN 1 THEN '李同学'
                WHEN 2 THEN '赵同学'
                WHEN 3 THEN '孙同学'
                ELSE '周同学'
                END,
            t.n
    ) AS name,
    'STUDENT' AS role,
    CASE MOD(t.n, 3)
        WHEN 0 THEN 'HIGH'
        WHEN 1 THEN 'LOW'
        ELSE 'MEDIUM'
        END AS risk_level,
    1 AS enabled,
    NOW() AS created_at
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
         UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
         UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
         UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
         UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
         UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
         UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
         UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
     ) t;

-- ----------------------------
-- 课程（teacher_id=2 不变）
-- ----------------------------
INSERT INTO course (id, course_code, course_name, teacher_id, credit, created_at) VALUES
                                                                                      (1, 'CS101', '程序设计基础', 2, 3, NOW()),
                                                                                      (2, 'MA101', '高等数学',     2, 4, NOW());

-- ----------------------------
-- 任务（created_by=2 不变）
-- ----------------------------
INSERT INTO task (id, course_id, type, title, details, due_date, created_by, created_at) VALUES
                                                                                             (1, 1, 'homework', '第一次编程作业', '实现排序算法并提交报告', DATE_ADD(NOW(), INTERVAL 3 DAY), 2, NOW()),
                                                                                             (2, 1, 'video',    'C语言指针专题视频', '观看至少30分钟并完成随堂题', DATE_ADD(NOW(), INTERVAL 2 DAY), 2, NOW()),
                                                                                             (3, 1, 'exam',     '第一章单元测验', '时长30分钟，满分100', DATE_ADD(NOW(), INTERVAL 5 DAY), 2, NOW()),
                                                                                             (4, 2, 'homework', '高数极限练习', '完成极限与连续性综合题', DATE_ADD(NOW(), INTERVAL 4 DAY), 2, NOW()),
                                                                                             (5, 2, 'video',    '导数应用专题视频', '观看至少25分钟并完成练习', DATE_ADD(NOW(), INTERVAL 3 DAY), 2, NOW()),
                                                                                             (6, 2, 'exam',     '高数章节测验', '时长45分钟，满分100', DATE_ADD(NOW(), INTERVAL 7 DAY), 2, NOW());

-- ----------------------------
-- 考试（保持你原来的 id=1,6）
-- ----------------------------
INSERT INTO exam (id, course_id, title, total_score, pass_score, start_time, end_time) VALUES
                                                                                           (1, 1, '程序设计基础-第一章测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
                                                                                           (6, 2, '高等数学-导数章节测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- =========================================================
-- 下面开始：只更新“与 user 相关”的表（给 50 个学生补齐数据）
-- 学生 id 范围：3..52
-- =========================================================

-- ----------------------------
-- 作业提交 homework_submission
-- homework_id：用 task.id (1/4)
-- ----------------------------
INSERT INTO homework_submission (id, homework_id, student_id, content, status, score, submitted_at)
SELECT
    (t.n) AS id,
    CASE WHEN t.n <= 25 THEN 1 ELSE 4 END AS homework_id,
    (2 + t.n) AS student_id,
    CONCAT('已提交：模拟数据，第', t.n, '号学生作业。') AS content,
    'SUBMITTED' AS status,
    -- 分数 45~95 波动
    (45 + MOD(t.n * 7, 51)) + 0.00 AS score,
    NOW() AS submitted_at
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
         UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
         UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
         UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
         UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
         UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
         UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
         UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
     ) t;

-- ----------------------------
-- 视频观看 video_watch_record
-- video_id：用 task.id (2/5)
-- ----------------------------
INSERT INTO video_watch_record (id, video_id, student_id, watch_time, last_watched_at)
SELECT
    t.n AS id,
    CASE WHEN t.n <= 25 THEN 2 ELSE 5 END AS video_id,
    (2 + t.n) AS student_id,
    -- 秒数：300~3000
    (300 + MOD(t.n * 123, 2701)) AS watch_time,
    NOW() AS last_watched_at
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
         UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
         UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
         UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
         UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
         UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
         UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
         UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
     ) t;

-- ----------------------------
-- 考试提交 exam_submission
-- exam_id：前 25 个学生做 exam=1，后 25 个学生做 exam=6
-- ----------------------------
INSERT INTO exam_submission (id, exam_id, student_id, answers, score, is_passed, submitted_at)
SELECT
    t.n AS id,
    CASE WHEN t.n <= 25 THEN 1 ELSE 6 END AS exam_id,
    (2 + t.n) AS student_id,
    '{"q1":"A","q2":"C","q3":"D"}' AS answers,
    -- 分数 30~98
    (30 + MOD(t.n * 11, 69)) + 0.00 AS score,
    CASE WHEN (30 + MOD(t.n * 11, 69)) >= 60 THEN 1 ELSE 0 END AS is_passed,
    NOW() AS submitted_at
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
         UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
         UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
         UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
         UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
         UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
         UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
         UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
     ) t;

-- ----------------------------
-- 日行为聚合 student_daily_activity（50 个学生各 1 条）
-- ----------------------------
INSERT INTO student_daily_activity (id, student_id, activity_date, login_count, video_minutes, homework_submitted, avg_score)
SELECT
    t.n AS id,
    (2 + t.n) AS student_id,
    CURDATE() AS activity_date,
    (1 + MOD(t.n, 6)) AS login_count,                 -- 1~6
    (5 + MOD(t.n * 3, 56)) AS video_minutes,          -- 5~60
    1 AS homework_submitted,
    (40 + MOD(t.n * 5, 56)) + 0.00 AS avg_score       -- 40~95
FROM (
         SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
         UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
         UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
         UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
         UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
         UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
         UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
         UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
     ) t;

-- ----------------------------
-- 风险记录 risk_record（每个学生 3 天趋势：共 150 条）
-- ----------------------------
INSERT INTO risk_record (id, student_id, calc_date, risk_score, risk_level, detail_json)
SELECT
    (d.day_idx * 1000 + t.n) AS id,
    (2 + t.n) AS student_id,
    CASE d.day_idx
        WHEN 0 THEN DATE_SUB(CURDATE(), INTERVAL 2 DAY)
        WHEN 1 THEN DATE_SUB(CURDATE(), INTERVAL 1 DAY)
        ELSE CURDATE()
        END AS calc_date,
    -- 0.10 ~ 0.95
    ROUND((0.10 + (MOD(t.n * 17 + d.day_idx * 13, 86) / 100.0)), 2) AS risk_score,
    CASE
        WHEN (0.10 + (MOD(t.n * 17 + d.day_idx * 13, 86) / 100.0)) >= 0.75 THEN 'HIGH'
        WHEN (0.10 + (MOD(t.n * 17 + d.day_idx * 13, 86) / 100.0)) >= 0.45 THEN 'MEDIUM'
        ELSE 'LOW'
        END AS risk_level,
    CONCAT('{"reason":"模拟生成：day=', d.day_idx, ',stu=', (2 + t.n), '"}') AS detail_json
FROM
    (
        SELECT 0 AS day_idx UNION ALL SELECT 1 UNION ALL SELECT 2
    ) d
        CROSS JOIN
    (
        SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
        UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
        UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
        UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
        UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
        UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
        UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
        UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
        UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
    ) t;
