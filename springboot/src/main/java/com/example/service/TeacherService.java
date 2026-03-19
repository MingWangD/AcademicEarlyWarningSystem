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
import java.util.List;
import java.util.Map;

@Service
public class TeacherService {
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final StudentService studentService;

    public TeacherService(TaskMapper taskMapper, ActivityMapper activityMapper, UserMapper userMapper, QuestionMapper questionMapper, StudentService studentService) {
        this.taskMapper = taskMapper;
        this.activityMapper = activityMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.studentService = studentService;
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

        int updated = 0;
        List<Map<String, Object>> samples = new ArrayList<>();
        for (Map<String, Object> row : features) {
            Long studentId = ((Number) row.get("studentId")).longValue();
            double credit = studentService.creditOf(studentId);
            RiskLevel level = credit >= 3 ? RiskLevel.LOW : (credit >= 2 ? RiskLevel.MEDIUM : RiskLevel.HIGH);
            double riskScore = Math.round((1 - (credit / 4.0)) * 1000.0) / 1000.0;
            activityMapper.saveRiskRecord(studentId, date, riskScore, level.name(), String.format("{\"credit\":%.2f}", credit));
            userMapper.updateRiskLevel(studentId, level.name());
            samples.add(Map.of("studentId", studentId, "credit", credit, "riskLevel", level.name()));
            updated++;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("updatedCount", updated);
        map.put("summary", "已按学分规则完成风险更新");
        map.put("rule", Map.of(
                "homeworkWeight", 0.4,
                "examWeight", 0.6,
                "totalCredit", 4,
                "lowRange", "3~4",
                "mediumRange", "2~3",
                "highRange", "<2"
        ));
        map.put("sample", samples.stream().limit(5).toList());
        return map;
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
