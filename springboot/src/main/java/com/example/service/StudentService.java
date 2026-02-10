package com.example.service;

import com.example.entity.AppUser;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudentService {
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;

    public StudentService(UserMapper userMapper, TaskMapper taskMapper, ActivityMapper activityMapper) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
    }

    public AppUser getInfo(Long studentId) {
        return userMapper.findById(studentId);
    }

    public Object getTasks() {
        return taskMapper.findAll();
    }

    public Map<String, Boolean> submitHomework(Long studentId, Long homeworkId, String content) {
        activityMapper.submitHomework(homeworkId, studentId, content);
        return Map.of("success", true);
    }

    public Map<String, Boolean> watchVideo(Long studentId, Long videoId, Integer watchTime) {
        activityMapper.upsertVideoWatch(videoId, studentId, watchTime);
        return Map.of("success", true);
    }

    public Map<String, Object> submitExam(Long studentId, Long examId, String answersJson) {
        int score = Math.min(100, 60 + (answersJson == null ? 0 : answersJson.length() % 40));
        boolean isPassed = score >= 60;
        activityMapper.submitExam(examId, studentId, answersJson, score, isPassed);
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("isPassed", isPassed);
        return result;
    }
}
