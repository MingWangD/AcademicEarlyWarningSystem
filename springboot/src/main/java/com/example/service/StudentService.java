package com.example.service;

import com.example.entity.AppUser;
import com.example.entity.Task;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * 学生任务列表：返回 task 基本信息 + 当前学生的完成状态
     */
    public List<Map<String, Object>> getTasks(Long studentId) {
        List<Task> tasks = taskMapper.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Task task : tasks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskId", task.getId());
            item.put("type", task.getType());
            item.put("title", task.getTitle());
            item.put("details", task.getDetails());
            item.put("dueDate", task.getDueDate());
            item.put("status", taskStatus(studentId, task));
            result.add(item);
        }
        return result;
    }

    private String taskStatus(Long studentId, Task task) {
        return switch (task.getType()) {
            case "homework" ->
                    activityMapper.countHomeworkSubmitted(task.getId(), studentId) > 0 ? "已提交" : "待完成";
            case "video" -> {
                Integer watched = activityMapper.videoWatchTime(task.getId(), studentId);
                int seconds = watched == null ? 0 : watched;
                yield seconds >= 300 ? "已完成" : "待观看"; // 这里用 300 秒作为完成阈值（你原逻辑）
            }
            case "exam" ->
                    activityMapper.countExamSubmitted(task.getId(), studentId) > 0 ? "已完成" : "待考试";
            default -> "待完成";
        };
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
        Task task = taskMapper.findById(examId);
        if (task == null) {
            throw new IllegalArgumentException("考试任务不存在");
        }

        // 确保 exam 表里有这条考试（你 mapper 里需要有 ensureExamExists 方法）
        activityMapper.ensureExamExists(examId, task.getCourseId(), task.getTitle());

        int score = Math.min(100, 60 + (answersJson == null ? 0 : answersJson.length() % 40));
        boolean isPassed = score >= 60;

        activityMapper.submitExam(examId, studentId, answersJson, score, isPassed);

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("isPassed", isPassed);
        result.put("success", true);
        return result;
    }
}
