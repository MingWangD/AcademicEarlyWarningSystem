package com.example.service;

import cn.hutool.json.JSONUtil;
import com.example.entity.AppUser;
import com.example.entity.Question;
import com.example.entity.Task;
import com.example.enums.RiskLevel;
import com.example.mapper.ActivityMapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentService {
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final QuestionMapper questionMapper;
    private final RiskAnalysisService riskAnalysisService;

    public StudentService(UserMapper userMapper, TaskMapper taskMapper, ActivityMapper activityMapper, QuestionMapper questionMapper, RiskAnalysisService riskAnalysisService) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.questionMapper = questionMapper;
        this.riskAnalysisService = riskAnalysisService;
    }

    public AppUser getInfo(Long studentId) {
        return userMapper.findById(studentId);
    }

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
            case "homework" -> activityMapper.countHomeworkSubmitted(task.getId(), studentId) > 0 ? "已提交" : "待完成";
            case "video" -> Optional.ofNullable(activityMapper.videoWatchTime(task.getId(), studentId)).orElse(0) >= 300 ? "已完成" : "待观看";
            case "exam" -> activityMapper.countExamSubmitted(task.getId(), studentId) > 0 ? "已完成" : "待考试";
            default -> "待完成";
        };
    }

    public List<Map<String, Object>> taskQuestions(Long taskId) {
        List<Question> questions = questionMapper.questionsByTaskId(taskId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("stem", q.getStem());
            item.put("optionA", q.getOptionA());
            item.put("optionB", q.getOptionB());
            item.put("optionC", q.getOptionC());
            item.put("optionD", q.getOptionD());
            list.add(item);
        }
        return list;
    }

    public Map<String, Object> submitHomeworkAnswers(Long studentId, Long taskId, Map<String, String> answers) {
        int score = scoreByAnswers(taskId, answers, 5);
        activityMapper.submitHomework(taskId, studentId, JSONUtil.toJsonStr(answers), score);
        activityMapper.upsertDailyHomework(studentId, score);
        RiskAnalysisService.RiskResult riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "score", score,
                "isPassed", score >= 60,
                "riskLevel", riskResult.level().name(),
                "riskScore", riskResult.score()
        );
    }

    public Map<String, Object> watchVideo(Long studentId, Long videoId, Integer watchTime) {
        activityMapper.upsertVideoWatch(videoId, studentId, watchTime);
        activityMapper.upsertDailyVideo(studentId, Math.max(1, watchTime / 60));
        RiskAnalysisService.RiskResult riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "riskLevel", riskResult.level().name(),
                "riskScore", riskResult.score()
        );
    }

    public Map<String, Object> highRiskStreak(Long studentId) {
        Integer hit = activityMapper.isHighRiskStreak7(studentId);
        return Map.of("high7days", hit != null && hit > 0);
    }

    public Map<String, Object> submitExamAnswers(Long studentId, Long taskId, Map<String, String> answers) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("考试任务不存在");
        }
        activityMapper.ensureExamExists(taskId, task.getCourseId(), task.getTitle(), java.time.LocalDateTime.now(), task.getDueDate());
        int score = scoreByAnswers(taskId, answers, 10);
        boolean isPassed = score >= 60;
        activityMapper.submitExam(taskId, studentId, JSONUtil.toJsonStr(answers), score, isPassed);
        activityMapper.upsertDailyExam(studentId, score);
        RiskAnalysisService.RiskResult riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "score", score,
                "isPassed", isPassed,
                "riskLevel", riskResult.level().name(),
                "riskScore", riskResult.score()
        );
    }

    private RiskAnalysisService.RiskResult refreshRiskLevel(Long studentId) {
        Map<String, Object> features = activityMapper.studentFeatureByDate(studentId, java.time.LocalDate.now());
        if (features == null || features.isEmpty()) {
            return new RiskAnalysisService.RiskResult(0.0, RiskLevel.LOW, "{}");
        }
        RiskAnalysisService.ModelProfile profile = riskAnalysisService.trainModel(java.time.LocalDate.now());
        RiskAnalysisService.RiskResult result = riskAnalysisService.evaluateStudent(features, profile);
        userMapper.updateRiskLevel(studentId, result.level().name());
        activityMapper.saveRiskRecord(studentId, java.time.LocalDate.now(), result.score(), result.level().name(), result.detailJson());
        return result;
    }

    private int scoreByAnswers(Long taskId, Map<String, String> answers, int totalCount) {
        List<Question> questions = questionMapper.questionsByTaskId(taskId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("该任务尚未生成题目");
        }
        int correct = 0;
        for (Question q : questions) {
            String picked = answers == null ? null : answers.get(String.valueOf(q.getId()));
            if (q.getCorrectAnswer().equalsIgnoreCase(String.valueOf(picked))) {
                correct++;
            }
        }
        return (int) Math.round((correct * 100.0) / Math.max(totalCount, questions.size()));
    }
}
