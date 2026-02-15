package com.example.service;

import com.example.dto.StudentRequests;
import com.example.entity.AppUser;
import com.example.entity.Task;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

@Service
public class AutoHomeworkScheduleService {

    private static final Long DEFAULT_TEACHER_ID = 2L;
    private static final Long DEFAULT_COURSE_ID = 1L;
    private static final int AUTO_SUBMIT_RATE_PERCENT = 70;

    private final TeacherService teacherService;
    private final StudentService studentService;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;

    public AutoHomeworkScheduleService(TeacherService teacherService,
                                       StudentService studentService,
                                       TaskMapper taskMapper,
                                       UserMapper userMapper,
                                       ActivityMapper activityMapper) {
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.activityMapper = activityMapper;
    }

    /**
     * 每天凌晨自动发布作业，并自动为学生生成提交记录。
     * 不影响教师在前端手动发布任务，两套流程可并行使用。
     */
    @Scheduled(cron = "0 20 0 * * ?")
    public void autoPublishAndSubmitHomework() {
        runForDate(LocalDate.now());
    }

    /**
     * 应用启动后做一次“当日补偿执行”：
     * 如果服务器在 00:20 之后才启动，也会补发今天的自动作业。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartupCompensation() {
        runForDate(LocalDate.now());
    }

    private void runForDate(LocalDate date) {
        String title = "自动作业-" + date;

        Task task = taskMapper.findByTitle(title);
        if (task == null) {
            StudentRequests.TeacherTaskCreateRequest req = new StudentRequests.TeacherTaskCreateRequest();
            req.setCourseId(DEFAULT_COURSE_ID);
            req.setType("homework");
            req.setTitle(title);
            req.setDetails("系统每日凌晨自动发布的练习作业");
            req.setDueDate(LocalDateTime.of(date.plusDays(1), LocalTime.of(23, 59)));
            Long taskId = teacherService.createTask(req, DEFAULT_TEACHER_ID);
            task = taskMapper.findById(taskId);
        }

        if (task == null) {
            return;
        }

        List<Map<String, Object>> qs = studentService.taskQuestions(task.getId());
        if (qs.isEmpty()) {
            return;
        }

        Map<String, String> answerTemplate = new HashMap<>();
        for (Map<String, Object> q : qs) {
            answerTemplate.put(String.valueOf(q.get("id")), "A");
        }

        List<AppUser> students = userMapper.findAllStudents();
        for (AppUser student : students) {
            if (!shouldAutoSubmit(date, student.getId())) {
                continue;
            }
            if (activityMapper.countHomeworkSubmitted(task.getId(), student.getId()) > 0) {
                continue;
            }
            studentService.submitHomeworkAnswers(student.getId(), task.getId(), answerTemplate);
        }
    }

    /**
     * 按“日期 + 学号”做稳定伪随机：每天约 70% 学生会被自动提交。
     * 同一天重复执行任务时结果稳定，避免忽高忽低。
     */
    private boolean shouldAutoSubmit(LocalDate date, Long studentId) {
        CRC32 crc32 = new CRC32();
        String key = date + "-" + studentId;
        crc32.update(key.getBytes(StandardCharsets.UTF_8));
        long bucket = crc32.getValue() % 100;
        return bucket < AUTO_SUBMIT_RATE_PERCENT;
    }
}
