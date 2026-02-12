package com.example.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class StudentRequests {
    public static class HomeworkSubmitRequest {
        private Long homeworkId;
        private String content;
        public Long getHomeworkId() { return homeworkId; }
        public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class VideoWatchRequest {
        private Long videoId;
        private Integer watchTime;
        public Long getVideoId() { return videoId; }
        public void setVideoId(Long videoId) { this.videoId = videoId; }
        public Integer getWatchTime() { return watchTime; }
        public void setWatchTime(Integer watchTime) { this.watchTime = watchTime; }
    }

    public static class ExamSubmitRequest {
        private Long examId;
        private Map<String, String> answers;
        public Long getExamId() { return examId; }
        public void setExamId(Long examId) { this.examId = examId; }
        public Map<String, String> getAnswers() { return answers; }
        public void setAnswers(Map<String, String> answers) { this.answers = answers; }
    }

    public static class TeacherTaskCreateRequest {
        private Long courseId;
        private String type;
        private String title;
        private String details;
        private LocalDateTime dueDate;
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    }

    public static class RiskCalcRequest {
        private LocalDate date;
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}
