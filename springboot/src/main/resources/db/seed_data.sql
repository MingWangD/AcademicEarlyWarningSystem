-- 学业预警系统测试数据（MySQL 5.7）
-- 用途：用于本地联调登录、任务查询、看板展示等功能
-- 注意：默认依赖 schema.sql 已执行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 清空（按外键依赖顺序）
-- ----------------------------
TRUNCATE TABLE risk_record;
TRUNCATE TABLE student_daily_activity;
TRUNCATE TABLE exam_submission;
TRUNCATE TABLE exam;
TRUNCATE TABLE video_watch_record;
TRUNCATE TABLE homework_submission;
TRUNCATE TABLE task;
TRUNCATE TABLE course;
TRUNCATE TABLE user;

-- ----------------------------
-- 用户（密码统一为 123456）
-- BCrypt hash: $2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK
-- ----------------------------
INSERT INTO user (id, username, password, email, name, role, risk_level, enabled, created_at) VALUES
(1, 'admin01',   '$2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK', 'admin01@school.edu',   '系统管理员', 'ADMIN',   'LOW',    1, NOW()),
(2, 'teacher01', '$2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK', 'teacher01@school.edu', '张老师',     'TEACHER', 'LOW',    1, NOW()),
(3, 'stu1001',   '$2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK', 'stu1001@school.edu',   '李同学',     'STUDENT', 'MEDIUM', 1, NOW()),
(4, 'stu1002',   '$2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK', 'stu1002@school.edu',   '王同学',     'STUDENT', 'LOW',    1, NOW()),
(5, 'stu1003',   '$2a$10$X5Q81Q7hA7vEzh6f6N0oWuQxi2P.DA7f4f8QhVfXQ4Wv0VJriM8lK', 'stu1003@school.edu',   '赵同学',     'STUDENT', 'HIGH',   1, NOW());

-- ----------------------------
-- 课程
-- ----------------------------
INSERT INTO course (id, course_code, course_name, teacher_id, credit, created_at) VALUES
(1, 'CS101', '程序设计基础', 2, 3, NOW()),
(2, 'MA101', '高等数学',     2, 4, NOW());

-- ----------------------------
-- 任务（作业/视频/考试）
-- ----------------------------
INSERT INTO task (id, course_id, type, title, details, due_date, created_by, created_at) VALUES
(1, 1, 'homework', '第一次编程作业', '实现排序算法并提交报告', DATE_ADD(NOW(), INTERVAL 3 DAY), 2, NOW()),
(2, 1, 'video',    'C语言指针专题视频', '观看至少30分钟并完成随堂题', DATE_ADD(NOW(), INTERVAL 2 DAY), 2, NOW()),
(3, 1, 'exam',     '第一章单元测验', '时长30分钟，满分100', DATE_ADD(NOW(), INTERVAL 5 DAY), 2, NOW());

-- ----------------------------
-- 考试
-- ----------------------------
INSERT INTO exam (id, course_id, title, total_score, pass_score, start_time, end_time) VALUES
(1, 1, '程序设计基础-第一章测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- ----------------------------
-- 学生行为/提交数据
-- ----------------------------
INSERT INTO homework_submission (id, homework_id, student_id, content, status, score, submitted_at) VALUES
(1, 1, 3, '已提交：包含冒泡排序与快速排序实现。', 'SUBMITTED', 88.00, NOW()),
(2, 1, 4, '已提交：包含插入排序实现。', 'SUBMITTED', 72.00, NOW());

INSERT INTO video_watch_record (id, video_id, student_id, watch_time, last_watched_at) VALUES
(1, 2, 3, 2100, NOW()),
(2, 2, 4, 1200, NOW()),
(3, 2, 5, 500,  NOW());

INSERT INTO exam_submission (id, exam_id, student_id, answers, score, is_passed, submitted_at) VALUES
(1, 1, 3, '{"q1":"A","q2":"C"}', 78.00, 1, NOW()),
(2, 1, 4, '{"q1":"B","q2":"D"}', 62.00, 1, NOW()),
(3, 1, 5, '{"q1":"A","q2":"B"}', 55.00, 0, NOW());

-- ----------------------------
-- 日行为聚合
-- ----------------------------
INSERT INTO student_daily_activity (id, student_id, activity_date, login_count, video_minutes, homework_submitted, avg_score) VALUES
(1, 3, CURDATE(), 4, 35, 1, 83.00),
(2, 4, CURDATE(), 2, 20, 1, 67.00),
(3, 5, CURDATE(), 1, 8,  0, 52.00);

-- ----------------------------
-- 风险记录
-- ----------------------------
INSERT INTO risk_record (id, student_id, calc_date, risk_score, risk_level, detail_json) VALUES
(1, 3, CURDATE(), 0.52, 'MEDIUM', '{"reason":"作业和考试中等"}'),
(2, 4, CURDATE(), 0.33, 'LOW',    '{"reason":"学习行为稳定"}'),
(3, 5, CURDATE(), 0.81, 'HIGH',   '{"reason":"作业缺交且分数偏低"}');

SET FOREIGN_KEY_CHECKS = 1;
