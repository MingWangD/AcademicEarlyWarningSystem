package com.example.service;

import com.example.dto.StudentRequests;
import com.example.entity.AppUser;
import com.example.entity.Task;
import com.example.entity.Question;
import com.example.enums.RiskLevel;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherService {
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final StudentService studentService;
    private final RiskAnalysisService riskAnalysisService;

    public TeacherService(TaskMapper taskMapper, ActivityMapper activityMapper, UserMapper userMapper,
                          QuestionMapper questionMapper, StudentService studentService,
                          RiskAnalysisService riskAnalysisService) {
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.studentService = studentService;
        this.riskAnalysisService = riskAnalysisService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createTask(StudentRequests.TeacherTaskCreateRequest request, Long teacherId) {
        List<Question> questions = List.of();
        if ("homework".equals(request.getType()) || "exam".equals(request.getType())) {
            String useType = "homework".equals(request.getType()) ? "HOMEWORK" : "EXAM";
            int count = "homework".equals(request.getType()) ? 5 : 10;
            questions = questionMapper.randomByCourseAndType(request.getCourseId(), useType, count);
            if (questions.isEmpty()) {
                throw new IllegalStateException("题库中暂无可用试题，请先维护题库后再发布任务");
            }
        }

        Task task = new Task();
        task.setCourseId(request.getCourseId());
        task.setType(request.getType());
        task.setTitle(request.getTitle());
        task.setDetails(request.getDetails());
        task.setDueDate(request.getDueDate());
        task.setCreatedBy(teacherId);
        taskMapper.insert(task);
        if ("homework".equals(task.getType()) || "exam".equals(task.getType())) {
            for (Question q : questions) {
                questionMapper.bindTaskQuestion(task.getId(), q.getId());
            }
        }
        if ("exam".equals(task.getType())) {
            activityMapper.ensureExamExists(task.getId(), task.getCourseId(), task.getTitle(), LocalDateTime.now(), task.getDueDate());
        }
        return task.getId();
    }

    public List<Map<String, Object>> activity(LocalDate date) {
        List<Map<String, Object>> rows = activityMapper.activitySummary(date);
        for (Map<String, Object> row : rows) {
            Long studentId = ((Number) row.get("studentId")).longValue();
            row.put("credit", studentService.creditOf(studentId));
        }
        return rows;
    }

    public Map<String, Object> info(Long teacherId) {
        AppUser user = userMapper.findById(teacherId);
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "credit", studentService.creditOf(teacherId)
        );
    }

    public Map<String, Object> calculateRisk(LocalDate date) {
        List<Map<String, Object>> features = activityMapper.featureRowsByDate(date);
        RiskAnalysisService.ModelProfile profile = riskAnalysisService.trainModel(date);

        int updated = 0;
        List<Map<String, Object>> samples = new ArrayList<>();
        for (Map<String, Object> row : features) {
            Long studentId = ((Number) row.get("studentId")).longValue();
            double credit = studentService.creditOf(studentId);
            double completionRate = Math.round((credit / 4.0) * 1000.0) / 1000.0;
            double failedCreditRatio = Math.round(Math.max(0, 1 - completionRate) * 1000.0) / 1000.0;

            RiskAnalysisService.RiskResult modelResult = riskAnalysisService.evaluateStudent(row, profile);
            RiskLevel level = applyCreditRules(modelResult.level(), completionRate, failedCreditRatio, credit);
            double riskScore = Math.round(Math.max(modelResult.score(), failedCreditRatio) * 1000.0) / 1000.0;

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("credit", credit);
            detail.put("creditCompletionRate", completionRate);
            detail.put("failedCreditRatio", failedCreditRatio);
            detail.put("riskReason", riskReason(completionRate, failedCreditRatio, credit));
            detail.put("model", modelResult.score());

            activityMapper.saveRiskRecord(studentId, date, riskScore, level.name(), cn.hutool.json.JSONUtil.toJsonStr(detail));
            userMapper.updateRiskLevel(studentId, level.name());
            samples.add(Map.of(
                    "studentId", studentId,
                    "credit", credit,
                    "creditCompletionRate", completionRate,
                    "failedCreditRatio", failedCreditRatio,
                    "riskLevel", level.name(),
                    "riskScore", riskScore
            ));
            updated++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("updatedCount", updated);
        map.put("summary", "已完成每日风险更新（学分特征+行为特征联合评估）");
        map.put("rule", Map.of(
                "homeworkWeight", 0.4,
                "examWeight", 0.6,
                "totalCredit", 4,
                "lowRange", "3~4",
                "mediumRange", "2~3",
                "highRange", "<2",
                "creditCompletionHighRiskThreshold", 0.7,
                "failedCreditHighRiskThreshold", 0.3
        ));
        map.put("modelParams", Map.of(
                "weights", profile.weights(),
                "bias", profile.bias(),
                "threshold", Map.of("medium", profile.mediumThreshold(), "high", profile.highThreshold()),
                "metrics", Map.of(
                        "precision", profile.metrics().precision(),
                        "recall", profile.metrics().recall(),
                        "f1", profile.metrics().f1(),
                        "accuracy", profile.metrics().accuracy()
                )
        ));
        map.put("sample", samples.stream().limit(5).toList());
        return map;
    }

    private RiskLevel applyCreditRules(RiskLevel baseLevel, double completionRate, double failedCreditRatio, double credit) {
        if (credit >= 3.0) {
            return RiskLevel.LOW;
        }
        if (credit < 2.0) {
            return RiskLevel.HIGH;
        }
        if (completionRate < 0.7 || failedCreditRatio > 0.3) {
            return RiskLevel.HIGH;
        }
        if (completionRate < 0.8) {
            return RiskLevel.MEDIUM;
        }
        return baseLevel;
    }

    private String riskReason(double completionRate, double failedCreditRatio, double credit) {
        if (completionRate < 0.7) return "学分完成率偏低";
        if (failedCreditRatio > 0.3) return "挂科学分占比偏高";
        if (credit < 2.0) return "当前学分不足";
        if (completionRate < 0.8) return "学分完成率接近预警阈值";
        return "学习状态稳定";
    }

    public Map<String, Object> dashboard(Long teacherId) {
        List<AppUser> students = userMapper.findAllStudents();
        long high = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.HIGH).count();
        long medium = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.MEDIUM).count();
        long low = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.LOW).count();

        LocalDate latestCalcDate = activityMapper.latestRiskCalcDateUntilToday();
        Map<String, Object> data = new HashMap<>();
        data.put("highRiskStudents", students.stream().filter(u -> u.getRiskLevel() == RiskLevel.HIGH).toList());
        data.put("mediumRiskStudents", students.stream().filter(u -> u.getRiskLevel() == RiskLevel.MEDIUM).toList());
        data.put("lowRiskStudents", students.stream().filter(u -> u.getRiskLevel() == RiskLevel.LOW).toList());
        data.put("riskDistribution", activityMapper.latestRiskDistribution());
        data.put("riskTrend", activityMapper.riskTrend());
        data.put("recentWarnings", activityMapper.recentWarningsLast7Days());
        data.put("summary", Map.of(
                "highCount", high,
                "mediumCount", medium,
                "lowCount", low,
                "totalStudents", students.size(),
                "refreshedAt", latestCalcDate == null ? "-" : latestCalcDate.toString(),
                "myCredit", studentService.creditOf(teacherId)
        ));
        return data;
    }
}
