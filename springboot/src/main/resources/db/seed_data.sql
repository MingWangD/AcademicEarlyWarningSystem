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
-- 题库 question_bank（补充作业/考试题，避免发布任务后无题可用）
-- course 1: 程序设计基础；course 2: 高等数学
-- ----------------------------
INSERT INTO question_bank (id, course_id, use_type, stem, option_a, option_b, option_c, option_d, correct_answer) VALUES
    -- course 1 homework (5)
    (1, 1, 'HOMEWORK', 'C语言中用于定义整型变量的关键字是？', 'char', 'int', 'float', 'double', 'B'),
    (2, 1, 'HOMEWORK', '以下哪种循环至少执行一次？', 'for', 'while', 'do...while', 'foreach', 'C'),
    (3, 1, 'HOMEWORK', '数组下标在C语言中默认从几开始？', '0', '1', '-1', '任意值', 'A'),
    (4, 1, 'HOMEWORK', '函数返回值类型为无返回应使用？', 'null', 'void', 'empty', 'none', 'B'),
    (5, 1, 'HOMEWORK', '指针变量存储的是？', '变量值', '地址', '函数名', '常量', 'B'),
    -- course 1 exam (10)
    (6, 1, 'EXAM', '以下哪项是合法的C变量名？', '2name', '_name', 'name-1', 'float', 'B'),
    (7, 1, 'EXAM', 'sizeof(int)的结果类型通常是？', 'int', 'long', 'size_t', 'char', 'C'),
    (8, 1, 'EXAM', 'break语句可用于？', 'if语句', 'switch和循环', '函数定义', '结构体定义', 'B'),
    (9, 1, 'EXAM', '字符串常量"abc"的长度是？', '2', '3', '4', '5', 'B'),
    (10, 1, 'EXAM', '下列哪个不是基本数据类型？', 'int', 'char', 'string', 'double', 'C'),
    (11, 1, 'EXAM', 'malloc分配内存后应配对使用？', 'delete', 'remove', 'free', 'clear', 'C'),
    (12, 1, 'EXAM', '++i 与 i++ 的主要区别是？', '结果相同无区别', '返回值时机不同', '类型不同', '作用域不同', 'B'),
    (13, 1, 'EXAM', '头文件stdio.h主要用于？', '字符串处理', '标准输入输出', '数学计算', '时间处理', 'B'),
    (14, 1, 'EXAM', '表达式 5/2 在C中结果是？', '2.5', '2', '3', '1', 'B'),
    (15, 1, 'EXAM', '以下哪项可防止数组越界？', '不声明长度', '边界检查', '使用goto', '使用宏替换', 'B'),
    -- course 2 homework (5)
    (16, 2, 'HOMEWORK', '函数 y=x^2 在 x=1 处导数为？', '1', '2', '3', '4', 'B'),
    (17, 2, 'HOMEWORK', '极限 lim(x->0) sinx/x 等于？', '0', '1', '不存在', '∞', 'B'),
    (18, 2, 'HOMEWORK', '函数在某点可导一定？', '连续', '有界', '可积', '单调', 'A'),
    (19, 2, 'HOMEWORK', '定积分几何意义常表示？', '斜率', '面积', '体积', '长度', 'B'),
    (20, 2, 'HOMEWORK', 'e^x 的导数是？', 'x*e^(x-1)', 'e^x', 'lnx', '1/x', 'B'),
    -- course 2 exam (10)
    (21, 2, 'EXAM', '函数 ln(x) 的定义域是？', 'x>=0', 'x>0', 'x!=0', '全体实数', 'B'),
    (22, 2, 'EXAM', 'lim(x->∞) 1/x 的值是？', '1', '0', '∞', '不存在', 'B'),
    (23, 2, 'EXAM', '若f''(x)>0，则f(x)在区间上？', '递减', '递增', '有极值', '震荡', 'B'),
    (24, 2, 'EXAM', '∫0^1 x dx 的值是？', '1', '1/2', '2', '1/3', 'B'),
    (25, 2, 'EXAM', '导数的定义本质上是？', '平均值', '极限', '面积', '级数', 'B'),
    (26, 2, 'EXAM', '函数 y=sinx 在 x=0 处导数为？', '0', '1', '-1', '不存在', 'B'),
    (27, 2, 'EXAM', '二阶导数反映函数的？', '斜率', '凹凸性', '积分区间', '定义域', 'B'),
    (28, 2, 'EXAM', '不定积分结果一般要加上？', '0', '常数C', '变量x', '绝对值', 'B'),
    (29, 2, 'EXAM', '若极限存在，则左右极限？', '至少一个存在', '都存在且相等', '都不存在', '必为0', 'B'),
    (30, 2, 'EXAM', '函数 y=x^3 在 x=2 处导数为？', '6', '8', '12', '4', 'C');

-- ----------------------------
-- 任务题目绑定 task_question
-- homework 任务各绑定 5 题；exam 任务各绑定 10 题
-- ----------------------------
INSERT INTO task_question (id, task_id, question_id) VALUES
    (1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5),
    (6, 3, 6), (7, 3, 7), (8, 3, 8), (9, 3, 9), (10, 3, 10),
    (11, 3, 11), (12, 3, 12), (13, 3, 13), (14, 3, 14), (15, 3, 15),
    (16, 4, 16), (17, 4, 17), (18, 4, 18), (19, 4, 19), (20, 4, 20),
    (21, 6, 21), (22, 6, 22), (23, 6, 23), (24, 6, 24), (25, 6, 25),
    (26, 6, 26), (27, 6, 27), (28, 6, 28), (29, 6, 29), (30, 6, 30);

-- ----------------------------
-- 考试（id 对齐 exam 类型 task.id：3,6）
-- ----------------------------
INSERT INTO exam (id, course_id, title, total_score, pass_score, start_time, end_time) VALUES
                                                                                           (3, 1, '程序设计基础-第一章测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY)),
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
-- exam_id：前 25 个学生做 exam=3，后 25 个学生做 exam=6
-- ----------------------------
INSERT INTO exam_submission (id, exam_id, student_id, answers, score, is_passed, submitted_at)
SELECT
    t.n AS id,
    CASE WHEN t.n <= 25 THEN 3 ELSE 6 END AS exam_id,
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
-- 扩展历史数据（2026-02-01 ~ 2026-02-13）
-- 目的：补充“作业完成 + 视频观看”历史行为，便于展示趋势
-- ----------------------------

-- 历史作业提交：每天每位学生 1 条
INSERT INTO homework_submission (homework_id, student_id, content, status, score, submitted_at)
SELECT
    CASE WHEN MOD(t.n + d.day_num, 2) = 0 THEN 1 ELSE 4 END AS homework_id,
    (2 + t.n) AS student_id,
    CONCAT('历史补充数据：2026-02-', LPAD(d.day_num, 2, '0'), ' 第', t.n, '号学生作业提交') AS content,
    'SUBMITTED' AS status,
    (55 + MOD(t.n * 9 + d.day_num * 7, 41)) + 0.00 AS score,
    STR_TO_DATE(CONCAT('2026-02-', LPAD(d.day_num, 2, '0'), ' 20:00:00'), '%Y-%m-%d %H:%i:%s') AS submitted_at
FROM (
         SELECT 1 AS day_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13
     ) d
CROSS JOIN (
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

-- 历史日行为聚合：补齐 2.1~2.13 的登录/视频/作业/分数数据（每位学生每天 1 条）
INSERT INTO student_daily_activity (student_id, activity_date, login_count, video_minutes, homework_submitted, avg_score)
SELECT
    (2 + t.n) AS student_id,
    STR_TO_DATE(CONCAT('2026-02-', LPAD(d.day_num, 2, '0')), '%Y-%m-%d') AS activity_date,
    (1 + MOD(t.n + d.day_num, 6)) AS login_count,
    (8 + MOD(t.n * 4 + d.day_num * 5, 53)) AS video_minutes,
    1 AS homework_submitted,
    (58 + MOD(t.n * 5 + d.day_num * 3, 38)) + 0.00 AS avg_score
FROM (
         SELECT 1 AS day_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
         UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13
     ) d
CROSS JOIN (
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
     ) t
ON DUPLICATE KEY UPDATE
    login_count = VALUES(login_count),
    video_minutes = VALUES(video_minutes),
    homework_submitted = VALUES(homework_submitted),
    avg_score = VALUES(avg_score);

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
