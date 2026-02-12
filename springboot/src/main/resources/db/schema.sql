-- 学业预警系统数据库设计（MySQL 5.7）

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(128),
    name VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL COMMENT 'STUDENT/TEACHER/ADMIN',
    risk_level VARCHAR(16) DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
    enabled TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_risk (risk_level)
);

CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(32) NOT NULL UNIQUE,
    course_name VARCHAR(128) NOT NULL,
    teacher_id BIGINT NOT NULL,
    credit INT DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL COMMENT 'video/homework/exam',
    title VARCHAR(128) NOT NULL,
    details TEXT,
    due_date DATETIME,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_course FOREIGN KEY (course_id) REFERENCES course(id),
    CONSTRAINT fk_task_creator FOREIGN KEY (created_by) REFERENCES user(id),
    INDEX idx_task_course_type_due (course_id, type, due_date)
);

CREATE TABLE IF NOT EXISTS homework_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    homework_id BIGINT NOT NULL COMMENT '对应 task.id 且 task.type=homework',
    student_id BIGINT NOT NULL,
    content TEXT,
    status VARCHAR(16) DEFAULT 'SUBMITTED',
    score DECIMAL(5,2) DEFAULT NULL,
    submitted_at DATETIME,
    INDEX idx_hw_student_time (student_id, submitted_at),
    CONSTRAINT fk_hw_student FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS video_watch_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id BIGINT NOT NULL COMMENT '对应 task.id 且 task.type=video',
    student_id BIGINT NOT NULL,
    watch_time INT DEFAULT 0 COMMENT '秒',
    last_watched_at DATETIME,
    UNIQUE KEY uk_video_student (video_id, student_id),
    INDEX idx_video_student_time (student_id, last_watched_at),
    CONSTRAINT fk_video_student FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS exam (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    total_score INT DEFAULT 100,
    pass_score INT DEFAULT 60,
    start_time DATETIME,
    end_time DATETIME,
    CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE IF NOT EXISTS exam_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    answers TEXT,
    score DECIMAL(5,2) NOT NULL,
    is_passed TINYINT(1) NOT NULL,
    submitted_at DATETIME,
    INDEX idx_exam_student_time (student_id, submitted_at),
    CONSTRAINT fk_es_exam FOREIGN KEY (exam_id) REFERENCES exam(id),
    CONSTRAINT fk_es_student FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS student_daily_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    login_count INT DEFAULT 0,
    video_minutes INT DEFAULT 0,
    homework_submitted INT DEFAULT 0,
    avg_score DECIMAL(5,2) DEFAULT 0,
    UNIQUE KEY uk_activity_student_day (student_id, activity_date),
    CONSTRAINT fk_daily_student FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS risk_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    calc_date DATE NOT NULL,
    risk_score DECIMAL(8,6) NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    detail_json TEXT,
    INDEX idx_risk_date_level (calc_date, risk_level),
    CONSTRAINT fk_risk_student FOREIGN KEY (student_id) REFERENCES user(id)
);
