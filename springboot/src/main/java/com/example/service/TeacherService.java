package com.example.service;

import com.example.dto.StudentRequests;
import com.example.entity.AppUser;
import com.example.entity.Task;
import com.example.enums.RiskLevel;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

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
    private final RiskAnalysisService riskAnalysisService;

    public TeacherService(TaskMapper taskMapper, ActivityMapper activityMapper, UserMapper userMapper, RiskAnalysisService riskAnalysisService) {
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.riskAnalysisService = riskAnalysisService;

    public TeacherService(TaskMapper taskMapper, ActivityMapper activityMapper, UserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
    }

    public Long createTask(StudentRequests.TeacherTaskCreateRequest request, Long teacherId) {
        Task task = new Task();
        task.setCourseId(request.getCourseId());
        task.setType(request.getType());
        task.setTitle(request.getTitle());
        task.setDetails(request.getDetails());
        task.setDueDate(request.getDueDate());
        task.setCreatedBy(teacherId);
        taskMapper.insert(task);
        return task.getId();
    }

    public List<Map<String, Object>> activity(LocalDate date) {
        return activityMapper.activitySummary(date);
    }

    public Map<String, Object> calculateRisk(LocalDate date) {
        List<Map<String, Object>> features = activityMapper.featureRowsByDate(date);
        double[] tuned = riskAnalysisService.tuneByCrossValidation(date);

        int updated = 0;
        for (Map<String, Object> row : features) {
            Long studentId = ((Number) row.get("studentId")).longValue();
            RiskAnalysisService.RiskResult result = riskAnalysisService.evaluateStudent(row, tuned);
            activityMapper.saveRiskRecord(studentId, date, result.score(), result.level().name(), result.detailJson());
            userMapper.updateRiskLevel(studentId, result.level().name());
        List<AppUser> students = userMapper.findAllStudents();
        int updated = 0;
        for (AppUser student : students) {
            double score = logisticScore(student.getId());
            RiskLevel level = score >= 0.7 ? RiskLevel.HIGH : score >= 0.4 ? RiskLevel.MEDIUM : RiskLevel.LOW;
            activityMapper.saveRiskRecord(student.getId(), date, score, level.name());
            userMapper.updateRiskLevel(student.getId(), level.name());
            updated++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("updatedCount", updated);
        map.put("summary", "已完成每日风险更新");
        map.put("modelParams", Map.of(
                "bias", tuned[0],
                "scoreWeight", tuned[1],
                "examFailWeight", tuned[2],
                "homeworkWeight", tuned[3],
                "videoWeight", tuned[4],
                "loginWeight", tuned[5]
        ));
        return map;
    }

    public Map<String, Object> dashboard() {
        List<AppUser> students = userMapper.findAllStudents();
        long high = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.HIGH).count();
        long medium = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.MEDIUM).count();
        long low = students.stream().filter(u -> u.getRiskLevel() == RiskLevel.LOW).count();

        Map<String, Object> data = new HashMap<>();
        data.put("highRiskStudents", students.stream().filter(u -> u.getRiskLevel() == RiskLevel.HIGH).toList());
        data.put("mediumRiskStudents", students.stream().filter(u -> u.getRiskLevel() == RiskLevel.MEDIUM).toList());
        data.put("riskDistribution", activityMapper.riskDistribution());
        data.put("riskTrend", activityMapper.riskTrend());
        data.put("recentWarnings", activityMapper.recentWarnings());
        data.put("summary", Map.of(
                "highCount", high,
                "mediumCount", medium,
                "lowCount", low,
                "totalStudents", students.size(),
                "refreshedAt", LocalDateTime.now().toString()
        ));
        return map;
    }

    private double logisticScore(Long studentId) {
        double x = (studentId % 10) / 10.0;
        return 1.0 / (1.0 + Math.exp(-(-1.5 + 3.2 * x)));
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("riskDistribution", activityMapper.riskDistribution());
        data.put("riskTrend", activityMapper.riskTrend());
        data.put("recentWarnings", activityMapper.recentWarnings());
        data.put("highRiskStudents", userMapper.findAllStudents().stream().filter(u -> u.getRiskLevel() == RiskLevel.HIGH).toList());
        data.put("mediumRiskStudents", userMapper.findAllStudents().stream().filter(u -> u.getRiskLevel() == RiskLevel.MEDIUM).toList());
        return data;
    }
}
