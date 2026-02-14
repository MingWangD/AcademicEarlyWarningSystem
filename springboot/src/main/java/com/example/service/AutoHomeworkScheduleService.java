package com.example.service;

import com.example.dto.StudentRequests;
import com.example.entity.AppUser;
import com.example.entity.Task;
import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AutoHomeworkScheduleService {

    private static final Long DEFAULT_TEACHER_ID = 2L;
    private static final Long DEFAULT_COURSE_ID = 1L;

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
        LocalDate today = LocalDate.now();
        String title = "自动作业-" + today;

        Task task = taskMapper.findByTitle(title);
        if (task == null) {
            StudentRequests.TeacherTaskCreateRequest req = new StudentRequests.TeacherTaskCreateRequest();
            req.setCourseId(DEFAULT_COURSE_ID);
            req.setType("homework");
            req.setTitle(title);
            req.setDetails("系统每日凌晨自动发布的练习作业");
            req.setDueDate(LocalDateTime.of(today.plusDays(1), LocalTime.of(23, 59)));
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
            if (activityMapper.countHomeworkSubmitted(task.getId(), student.getId()) > 0) {
                continue;
            }
            studentService.submitHomeworkAnswers(student.getId(), task.getId(), answerTemplate);
        }
    }
}
