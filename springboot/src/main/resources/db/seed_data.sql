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
TRUNCATE TABLE task_question;
TRUNCATE TABLE question_bank;
TRUNCATE TABLE task;
TRUNCATE TABLE course;
TRUNCATE TABLE user;

-- ----------------------------
-- 用户（密码统一为 123456）
-- BCrypt hash: $2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6
-- ----------------------------
INSERT INTO user (id, username, password, email, name, role, risk_level, enabled, created_at) VALUES
(1, 'admin01',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'admin01@school.edu',   '系统管理员', 'ADMIN',   'LOW',    1, NOW()),
(2, 'teacher01', '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'teacher01@school.edu', '张老师',     'TEACHER', 'LOW',    1, NOW()),
(3, 'stu1001',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1001@school.edu',   '李同学',     'STUDENT', 'MEDIUM', 1, NOW()),
(4, 'stu1002',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1002@school.edu',   '王同学',     'STUDENT', 'LOW',    1, NOW()),
(5, 'stu1003',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1003@school.edu',   '赵同学',     'STUDENT', 'HIGH',   1, NOW()),
(6, 'stu1004',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1004@school.edu',   '孙同学',     'STUDENT', 'LOW',    1, NOW()),
(7, 'stu1005',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1005@school.edu',   '周同学',     'STUDENT', 'MEDIUM', 1, NOW()),
(8, 'stu1006',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1006@school.edu',   '吴同学',     'STUDENT', 'LOW',    1, NOW()),
(9, 'stu1007',   '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1007@school.edu',   '郑同学',     'STUDENT', 'HIGH',   1, NOW()),
(10, 'stu1008',  '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1008@school.edu',   '冯同学',     'STUDENT', 'MEDIUM', 1, NOW()),
(11, 'stu1009',  '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1009@school.edu',   '陈同学',     'STUDENT', 'LOW',    1, NOW()),
(12, 'stu1010',  '$2a$10$aHdWzkYPJToxPxAb5rlr3eZe7hvCJBgSOyz3oxBgIfy0AjE2jgGY6', 'stu1010@school.edu',   '褚同学',     'STUDENT', 'HIGH',   1, NOW());

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
(3, 1, 'exam',     '第一章单元测验', '时长30分钟，满分100', DATE_ADD(NOW(), INTERVAL 5 DAY), 2, NOW()),
(4, 2, 'homework', '高数极限练习', '完成极限与连续性综合题', DATE_ADD(NOW(), INTERVAL 4 DAY), 2, NOW()),
(5, 2, 'video',    '导数应用专题视频', '观看至少25分钟并完成练习', DATE_ADD(NOW(), INTERVAL 3 DAY), 2, NOW()),
(6, 2, 'exam',     '高数章节测验', '时长45分钟，满分100', DATE_ADD(NOW(), INTERVAL 7 DAY), 2, NOW());

-- ----------------------------
-- 考试
-- ----------------------------
INSERT INTO exam (id, course_id, title, total_score, pass_score, start_time, end_time) VALUES
(1, 1, '程序设计基础-第一章测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
(6, 2, '高等数学-导数章节测验', 100, 60, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- ----------------------------
-- 学生行为/提交数据
-- ----------------------------
INSERT INTO homework_submission (id, homework_id, student_id, content, status, score, submitted_at) VALUES
(1, 1, 3,  '已提交：包含冒泡排序与快速排序实现。', 'SUBMITTED', 88.00, NOW()),
(2, 1, 4,  '已提交：包含插入排序实现。', 'SUBMITTED', 72.00, NOW()),
(3, 1, 6,  '已提交：包含归并排序与复杂度分析。', 'SUBMITTED', 91.00, NOW()),
(4, 1, 8,  '已提交：仅完成基础排序题。', 'SUBMITTED', 66.00, NOW()),
(5, 4, 7,  '已提交：极限证明与图像分析。', 'SUBMITTED', 79.00, NOW()),
(6, 4, 9,  '已提交：题目缺少步骤说明。', 'SUBMITTED', 58.00, NOW()),
(7, 4, 10, '已提交：完整解答并附推导过程。', 'SUBMITTED', 86.00, NOW()),
(8, 4, 11, '已提交：解答规范，计算准确。', 'SUBMITTED', 93.00, NOW());

INSERT INTO video_watch_record (id, video_id, student_id, watch_time, last_watched_at) VALUES
(1, 2, 3,  2100, NOW()),
(2, 2, 4,  1200, NOW()),
(3, 2, 5,   500, NOW()),
(4, 2, 6,  2600, NOW()),
(5, 2, 7,  1500, NOW()),
(6, 5, 8,  1700, NOW()),
(7, 5, 9,   600, NOW()),
(8, 5, 10, 2200, NOW()),
(9, 5, 11, 2600, NOW()),
(10,5, 12, 400, NOW());

INSERT INTO exam_submission (id, exam_id, student_id, answers, score, is_passed, submitted_at) VALUES
(1, 1, 3,  '{"q1":"A","q2":"C","q3":"D"}', 78.00, 1, NOW()),
(2, 1, 4,  '{"q1":"B","q2":"D","q3":"A"}', 62.00, 1, NOW()),
(3, 1, 5,  '{"q1":"A","q2":"B","q3":"C"}', 55.00, 0, NOW()),
(4, 1, 6,  '{"q1":"C","q2":"B","q3":"A"}', 90.00, 1, NOW()),
(5, 6, 7,  '{"q1":"D","q2":"A","q3":"B"}', 74.00, 1, NOW()),
(6, 6, 9,  '{"q1":"A","q2":"A","q3":"A"}', 48.00, 0, NOW()),
(7, 6, 10, '{"q1":"C","q2":"D","q3":"B"}', 81.00, 1, NOW()),
(8, 6, 11, '{"q1":"B","q2":"C","q3":"D"}', 89.00, 1, NOW());

-- ----------------------------
-- 日行为聚合
-- ----------------------------
INSERT INTO student_daily_activity (id, student_id, activity_date, login_count, video_minutes, homework_submitted, avg_score) VALUES
(1, 3,  CURDATE(), 4, 35, 1, 83.00),
(2, 4,  CURDATE(), 2, 20, 1, 67.00),
(3, 5,  CURDATE(), 1, 8,  0, 52.00),
(4, 6,  CURDATE(), 5, 43, 1, 90.00),
(5, 7,  CURDATE(), 3, 25, 1, 76.00),
(6, 8,  CURDATE(), 2, 28, 1, 66.00),
(7, 9,  CURDATE(), 1, 10, 1, 53.00),
(8, 10, CURDATE(), 4, 36, 1, 84.00),
(9, 11, CURDATE(), 5, 44, 1, 91.00),
(10,12, CURDATE(), 1, 6,  0, 49.00);

-- ----------------------------
-- 风险记录（含近7天趋势）
-- ----------------------------
INSERT INTO risk_record (id, student_id, calc_date, risk_score, risk_level, detail_json) VALUES
(1, 3,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.64, 'MEDIUM', '{"reason":"起始周学习状态一般"}'),
(2, 4,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.45, 'MEDIUM', '{"reason":"提交不够稳定"}'),
(3, 5,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.90, 'HIGH',   '{"reason":"连续缺交作业"}'),
(4, 6,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.28, 'LOW',    '{"reason":"基础较好"}'),
(5, 7,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.58, 'MEDIUM', '{"reason":"视频学习不足"}'),
(6, 8,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.54, 'MEDIUM', '{"reason":"成绩波动"}'),
(7, 9,  DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.91, 'HIGH',   '{"reason":"考试未通过"}'),
(8, 10, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.52, 'MEDIUM', '{"reason":"学习投入一般"}'),
(9, 11, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.30, 'LOW',    '{"reason":"整体稳定"}'),
(10,12, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 0.93, 'HIGH',   '{"reason":"登录极少"}'),

(11,3,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.61, 'MEDIUM', '{"reason":"作业质量提升"}'),
(12,4,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.42, 'MEDIUM', '{"reason":"状态改善"}'),
(13,5,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.88, 'HIGH',   '{"reason":"考试不及格"}'),
(14,6,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.25, 'LOW',    '{"reason":"持续良好"}'),
(15,7,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.55, 'MEDIUM', '{"reason":"提交延迟"}'),
(16,8,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.51, 'MEDIUM', '{"reason":"波动仍在"}'),
(17,9,   DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.89, 'HIGH',   '{"reason":"考试和作业偏弱"}'),
(18,10,  DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.49, 'MEDIUM', '{"reason":"投入中等"}'),
(19,11,  DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.27, 'LOW',    '{"reason":"学习行为优秀"}'),
(20,12,  DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0.91, 'HIGH',   '{"reason":"低活跃"}'),

(21,3,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.60, 'MEDIUM', '{"reason":"成绩保持中等"}'),
(22,4,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.40, 'LOW',    '{"reason":"行为趋稳"}'),
(23,5,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.87, 'HIGH',   '{"reason":"考试表现偏低"}'),
(24,6,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.23, 'LOW',    '{"reason":"成绩与活跃良好"}'),
(25,7,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.52, 'MEDIUM', '{"reason":"练习次数偏少"}'),
(26,8,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.49, 'MEDIUM', '{"reason":"需继续巩固"}'),
(27,9,   DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.87, 'HIGH',   '{"reason":"风险仍高"}'),
(28,10,  DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.46, 'MEDIUM', '{"reason":"状态一般"}'),
(29,11,  DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.25, 'LOW',    '{"reason":"稳定"}'),
(30,12,  DATE_SUB(CURDATE(), INTERVAL 4 DAY), 0.90, 'HIGH',   '{"reason":"学习时长偏低"}'),

(31,3,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.59, 'MEDIUM', '{"reason":"作业和考试中等"}'),
(32,4,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.39, 'LOW',    '{"reason":"学习行为稳定"}'),
(33,5,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.85, 'HIGH',   '{"reason":"作业缺交且分数偏低"}'),
(34,6,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.22, 'LOW',    '{"reason":"成绩与活跃度良好"}'),
(35,7,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.50, 'MEDIUM', '{"reason":"考试边缘通过"}'),
(36,8,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.47, 'MEDIUM', '{"reason":"作业质量一般"}'),
(37,9,   DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.86, 'HIGH',   '{"reason":"考试不及格"}'),
(38,10,  DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.43, 'MEDIUM', '{"reason":"仍需提升稳定性"}'),
(39,11,  DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.23, 'LOW',    '{"reason":"学习行为优秀"}'),
(40,12,  DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0.88, 'HIGH',   '{"reason":"长期低活跃"}'),

(41,3,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.58, 'MEDIUM', '{"reason":"作业和考试中等"}'),
(42,4,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.38, 'LOW',    '{"reason":"学习行为稳定"}'),
(43,5,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.84, 'HIGH',   '{"reason":"作业缺交且分数偏低"}'),
(44,6,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.21, 'LOW',    '{"reason":"成绩与活跃度良好"}'),
(45,7,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.49, 'MEDIUM', '{"reason":"考试边缘通过"}'),
(46,8,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.46, 'MEDIUM', '{"reason":"作业质量一般"}'),
(47,9,   DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.86, 'HIGH',   '{"reason":"考试不及格"}'),
(48,10,  DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.41, 'MEDIUM', '{"reason":"仍需提升稳定性"}'),
(49,11,  DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.22, 'LOW',    '{"reason":"学习行为优秀"}'),
(50,12,  DATE_SUB(CURDATE(), INTERVAL 2 DAY), 0.89, 'HIGH',   '{"reason":"长期低活跃"}'),

(51,3,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.55, 'MEDIUM', '{"reason":"成绩保持中等"}'),
(52,4,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.35, 'LOW',    '{"reason":"行为稳定"}'),
(53,5,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.82, 'HIGH',   '{"reason":"考试不及格"}'),
(54,6,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.19, 'LOW',    '{"reason":"持续良好"}'),
(55,7,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.47, 'MEDIUM', '{"reason":"需增加视频学习"}'),
(56,8,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.44, 'MEDIUM', '{"reason":"成绩波动"}'),
(57,9,   DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.83, 'HIGH',   '{"reason":"作业和考试偏弱"}'),
(58,10,  DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.39, 'LOW',    '{"reason":"状态改善"}'),
(59,11,  DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.20, 'LOW',    '{"reason":"保持优秀"}'),
(60,12,  DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0.87, 'HIGH',   '{"reason":"活跃度不足"}'),

(61,3,   CURDATE(), 0.52, 'MEDIUM', '{"reason":"作业和考试中等"}'),
(62,4,   CURDATE(), 0.33, 'LOW',    '{"reason":"学习行为稳定"}'),
(63,5,   CURDATE(), 0.81, 'HIGH',   '{"reason":"作业缺交且分数偏低"}'),
(64,6,   CURDATE(), 0.18, 'LOW',    '{"reason":"成绩与活跃度突出"}'),
(65,7,   CURDATE(), 0.45, 'MEDIUM', '{"reason":"考试通过但波动较大"}'),
(66,8,   CURDATE(), 0.42, 'MEDIUM', '{"reason":"建议增加练习"}'),
(67,9,   CURDATE(), 0.80, 'HIGH',   '{"reason":"考试连续低分"}'),
(68,10,  CURDATE(), 0.37, 'LOW',    '{"reason":"近期学习改善"}'),
(69,11,  CURDATE(), 0.19, 'LOW',    '{"reason":"表现稳定"}'),
(70,12,  CURDATE(), 0.85, 'HIGH',   '{"reason":"登录和学习时长偏低"}');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 题库（按课程ID生成作业/考试题）
-- ----------------------------
INSERT INTO question_bank (course_id,use_type,stem,option_a,option_b,option_c,option_d,correct_answer) VALUES
-- CS101 HOMEWORK（15题）
(1,'HOMEWORK','下列哪种排序算法平均时间复杂度为 O(n log n)','冒泡排序','快速排序','选择排序','插入排序','B'),
(1,'HOMEWORK','C语言中用于动态内存分配的函数是','printf','malloc','scanf','strlen','B'),
(1,'HOMEWORK','指针变量中存放的是','整型值','字符值','地址值','布尔值','C'),
(1,'HOMEWORK','以下哪个关键字用于定义常量','const','static','extern','typedef','A'),
(1,'HOMEWORK','递归函数必须具备的条件是','循环变量','终止条件','全局数组','结构体参数','B'),
(1,'HOMEWORK','下面哪种数据结构满足“先进后出”','队列','栈','链表','哈希表','B'),
(1,'HOMEWORK','二分查找适用于哪类序列','无序序列','随机序列','有序序列','链式序列','C'),
(1,'HOMEWORK','for(int i=0;i<10;i++) 中 i++ 表示','i减1','i乘2','i加1','i赋0','C'),
(1,'HOMEWORK','以下哪项属于面向对象三大特性之一','分页','封装','并发','编译','B'),
(1,'HOMEWORK','数组下标在 C 语言中默认从几开始','1','0','-1','任意值','B'),
(1,'HOMEWORK','若 a=5,b=2，则 a/b 在整型运算中的结果是','2.5','2','3','1','B'),
(1,'HOMEWORK','下面哪种遍历方式常用于二叉树层序遍历','深度优先','广度优先','中序遍历','后序遍历','B'),
(1,'HOMEWORK','链表相对数组的主要优势是','随机访问快','插入删除灵活','占用内存更少','支持并行','B'),
(1,'HOMEWORK','哈希冲突常见解决方法不包括','开放定址','链地址法','回溯法','再哈希','C'),
(1,'HOMEWORK','在函数参数传递中，C语言默认采用','引用传递','值传递','指针传递','对象传递','B'),

-- CS101 EXAM（15题）
(1,'EXAM','快速排序最坏情况下时间复杂度为','O(n)','O(n log n)','O(n^2)','O(log n)','C'),
(1,'EXAM','下面哪项会导致内存泄漏风险','free已分配内存','malloc后未释放','局部变量声明','数组初始化','B'),
(1,'EXAM','指针 p 指向整型变量，*p 表示','地址值','变量名','指向值','数据类型','C'),
(1,'EXAM','C语言中字符串结束标志是','换行符','字符串结束符','制表符','空格','B'),
(1,'EXAM','栈的基本操作不包括','push','pop','enqueue','peek','C'),
(1,'EXAM','下列哪个是稳定排序','快速排序','堆排序','归并排序','希尔排序','C'),
(1,'EXAM','算法复杂度主要用于衡量','运行效率','界面美观','数据库容量','网络协议','A'),
(1,'EXAM','链表节点通常包含','数据域和指针域','仅数据域','仅地址域','仅索引域','A'),
(1,'EXAM','若循环执行次数不确定，常用语句是','for','switch','while','case','C'),
(1,'EXAM','下列哪项属于逻辑运算符','+','&&','%','/','B'),
(1,'EXAM','深度优先遍历通常借助','队列','数组','栈','哈希','C'),
(1,'EXAM','对于长度为 n 的数组，最后一个下标是','n','n+1','n-1','1','C'),
(1,'EXAM','以下哪项不是面向对象语言常见特性','继承','多态','封装','编译优化开关','D'),
(1,'EXAM','在 C 语言中，sizeof(int) 返回的是','类型大小','变量值','地址长度','下标范围','A'),
(1,'EXAM','时间复杂度 O(1) 表示','常数级','线性级','平方级','对数级','A'),

-- MA101 HOMEWORK（12题）
(2,'HOMEWORK','函数 f(x)=x^2 在 x=1 处导数为','1','2','3','4','B'),
(2,'HOMEWORK','极限 lim(x->0) sinx/x 等于','0','1','不存在','∞','B'),
(2,'HOMEWORK','函数连续的必要条件之一是','左右极限都存在且相等','有最大值','有最小值','周期性','A'),
(2,'HOMEWORK','∫0^1 x dx 的值为','1','1/2','2','0','B'),
(2,'HOMEWORK','导数几何意义通常表示曲线的','面积','切线斜率','弧长','体积','B'),
(2,'HOMEWORK','函数单调递增常见判据是','导数大于0','导数小于0','导数等于0','三阶导数为0','A'),
(2,'HOMEWORK','下列属于初等函数的是','分段常值函数','指数函数','狄利克雷函数','取整函数','B'),
(2,'HOMEWORK','若矩阵可逆，则其行列式','等于0','不等于0','小于0','大于1','B'),
(2,'HOMEWORK','级数收敛的判别方法不包括','比较判别法','比值判别法','夹逼定理','主成分分析','D'),
(2,'HOMEWORK','函数 y=lnx 的定义域是','x>0','x>=0','全体实数','x<0','A'),
(2,'HOMEWORK','定积分的几何意义之一是','函数零点个数','曲边梯形面积','方程根的个数','向量长度','B'),
(2,'HOMEWORK','二元函数偏导数表示','整体变化率','对某一变量变化率','周期','极值点数量','B'),

-- MA101 EXAM（12题）
(2,'EXAM','lim(x->∞) 1/x 的值为','1','0','∞','不存在','B'),
(2,'EXAM','函数在某点可导则该点','必连续','必不连续','可能连续','与连续无关','A'),
(2,'EXAM','若某点导数等于0，则该点','一定极大值','一定极小值','可能是极值点','一定不是极值','C'),
(2,'EXAM','不定积分 ∫2x dx 等于','x^2+C','2x^2+C','x+C','lnx+C','A'),
(2,'EXAM','下列哪个级数发散','等比级数 |q|<1','调和级数','p级数 p>1','交错级数','B'),
(2,'EXAM','矩阵乘法满足','交换律总成立','结合律成立','分配律不成立','与数量积相同','B'),
(2,'EXAM','多元函数极值判定常用','一阶导和 Hessian','傅里叶级数','拉普拉斯变换','牛顿插值','A'),
(2,'EXAM','若函数在区间内连续，则','必有原函数','必有最大最小值','必可导','必单调','B'),
(2,'EXAM','微分中 dx 通常表示','函数值','自变量增量','常数','积分上限','B'),
(2,'EXAM','泰勒展开用于','近似函数','求逆矩阵','统计回归','图像压缩','A'),
(2,'EXAM','二重积分常用于求','曲面下体积','一元函数导数','向量叉积','概率密度','A'),
(2,'EXAM','常微分方程 dy/dx=y 的通解是','y=Cx','y=Ce^x','y=x^2+C','y=sinx+C','B');
