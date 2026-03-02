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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherService {
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final RiskAnalysisService riskAnalysisService;

    public TeacherService(TaskMapper taskMapper, ActivityMapper activityMapper, UserMapper userMapper, QuestionMapper questionMapper, RiskAnalysisService riskAnalysisService) {
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
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
        return activityMapper.activitySummary(date);
    }

    public Map<String, Object> calculateRisk(LocalDate date) {
        List<Map<String, Object>> features = activityMapper.featureRowsByDate(date);
        RiskAnalysisService.ModelProfile profile = riskAnalysisService.trainModel(date);

        int updated = 0;
        for (Map<String, Object> row : features) {
            Long studentId = ((Number) row.get("studentId")).longValue();
            RiskAnalysisService.RiskResult result = riskAnalysisService.evaluateStudent(row, profile);
            activityMapper.saveRiskRecord(studentId, date, result.score(), result.level().name(), result.detailJson());
            userMapper.updateRiskLevel(studentId, result.level().name());
            updated++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("updatedCount", updated);
        map.put("summary", "已完成每日风险更新");
        map.put("modelParams", Map.ofEntries(
                Map.entry("bias", profile.bias()),
                Map.entry("scoreWeight", profile.weights()[0]),
                Map.entry("examFailWeight", profile.weights()[1]),
                Map.entry("homeworkWeight", profile.weights()[2]),
                Map.entry("videoWeight", profile.weights()[3]),
                Map.entry("loginWeight", profile.weights()[4]),
                Map.entry("mediumThreshold", profile.mediumThreshold()),
                Map.entry("highThreshold", profile.highThreshold()),
                Map.entry("precision", profile.metrics().precision()),
                Map.entry("recall", profile.metrics().recall()),
                Map.entry("f1", profile.metrics().f1()),
                Map.entry("accuracy", profile.metrics().accuracy())
        ));
        return map;
    }

    public Map<String, Object> dashboard() {
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
                "refreshedAt", latestCalcDate == null ? "-" : latestCalcDate.toString()
        ));
        return data;
    }
}
