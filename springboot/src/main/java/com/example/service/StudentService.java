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
    private static final double HOMEWORK_WEIGHT = 0.4;
    private static final double EXAM_WEIGHT = 0.6;
    private static final double TOTAL_CREDIT = 4.0;

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final QuestionMapper questionMapper;

    public StudentService(UserMapper userMapper, TaskMapper taskMapper, ActivityMapper activityMapper, QuestionMapper questionMapper) {
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.questionMapper = questionMapper;
    }

    public Map<String, Object> getInfo(Long studentId) {
        AppUser user = userMapper.findById(studentId);
        RiskSnapshot snapshot = refreshRiskLevel(studentId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("name", user.getName());
        map.put("role", user.getRole());
        map.put("riskLevel", snapshot.riskLevel().name());
        map.put("credit", snapshot.credit());
        return map;
    }

    public Map<String, Object> getTasks(Long studentId, Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNum - 1) * safePageSize;
        List<Task> tasks = taskMapper.findPage(safePageSize, offset);
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
        long total = taskMapper.countAll();
        return Map.of(
                "list", result,
                "total", total,
                "pageNum", safePageNum,
                "pageSize", safePageSize
        );
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
        RiskSnapshot riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "score", score,
                "isPassed", score >= 60,
                "riskLevel", riskResult.riskLevel().name(),
                "riskScore", riskResult.riskScore(),
                "credit", riskResult.credit()
        );
    }

    public Map<String, Object> watchVideo(Long studentId, Long videoId, Integer watchTime) {
        activityMapper.upsertVideoWatch(videoId, studentId, watchTime);
        activityMapper.upsertDailyVideo(studentId, Math.max(1, watchTime / 60));
        RiskSnapshot riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "riskLevel", riskResult.riskLevel().name(),
                "riskScore", riskResult.riskScore(),
                "credit", riskResult.credit()
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
        RiskSnapshot riskResult = refreshRiskLevel(studentId);
        return Map.of(
                "success", true,
                "score", score,
                "isPassed", isPassed,
                "riskLevel", riskResult.riskLevel().name(),
                "riskScore", riskResult.riskScore(),
                "credit", riskResult.credit()
        );
    }

    public double creditOf(Long userId) {
        Double homeworkAvg = Optional.ofNullable(activityMapper.avgHomeworkScore(userId)).orElse(0d);
        Double examAvg = Optional.ofNullable(activityMapper.avgExamScore(userId)).orElse(0d);
        double weightedScore = homeworkAvg * HOMEWORK_WEIGHT + examAvg * EXAM_WEIGHT;
        double credit = (weightedScore / 100.0) * TOTAL_CREDIT;
        return Math.round(Math.max(0d, Math.min(TOTAL_CREDIT, credit)) * 100.0) / 100.0;
    }

    private RiskSnapshot refreshRiskLevel(Long studentId) {
        double credit = creditOf(studentId);
        RiskLevel level = credit >= 3 ? RiskLevel.LOW : (credit >= 2 ? RiskLevel.MEDIUM : RiskLevel.HIGH);
        double riskScore = Math.round((1 - (credit / TOTAL_CREDIT)) * 1000.0) / 1000.0;
        userMapper.updateRiskLevel(studentId, level.name());
        String detailJson = String.format("{\"credit\":%.2f,\"homeworkWeight\":%.1f,\"examWeight\":%.1f}", credit, HOMEWORK_WEIGHT, EXAM_WEIGHT);
        activityMapper.saveRiskRecord(studentId, java.time.LocalDate.now(), riskScore, level.name(), detailJson);
        return new RiskSnapshot(credit, riskScore, level);
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

    public record RiskSnapshot(double credit, double riskScore, RiskLevel riskLevel) {}
}
