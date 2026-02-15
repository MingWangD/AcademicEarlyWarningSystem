package com.example.api;

import com.example.common.ApiResponse;
import com.example.dto.StudentRequests;
import com.example.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/info")
    public ApiResponse<?> info(Authentication auth) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.getInfo(uid));
    }

    @GetMapping("/tasks")
    public ApiResponse<?> tasks(Authentication auth) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.getTasks(uid));
    }

    @GetMapping("/homework/{taskId}/questions")
    public ApiResponse<?> homeworkQuestions(@PathVariable Long taskId) {
        return ApiResponse.ok(studentService.taskQuestions(taskId));
    }

    @GetMapping("/exam/{taskId}/questions")
    public ApiResponse<?> examQuestions(@PathVariable Long taskId) {
        return ApiResponse.ok(studentService.taskQuestions(taskId));
    }

    @PostMapping("/homework/submit")
    public ApiResponse<?> homework(Authentication auth, @RequestBody StudentRequests.TaskAnswerSubmitRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.submitHomeworkAnswers(uid, request.getTaskId(), request.getAnswers()));
    }

    @PostMapping("/video/watch")
    public ApiResponse<?> watch(Authentication auth, @RequestBody StudentRequests.VideoWatchRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.watchVideo(uid, request.getVideoId(), request.getWatchTime()));
    }

    @GetMapping("/risk/high-streak")
    public ApiResponse<?> highRiskStreak(Authentication auth) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.highRiskStreak(uid));
    }

    @PostMapping("/exam/submit")
    public ApiResponse<?> exam(Authentication auth, @RequestBody StudentRequests.TaskAnswerSubmitRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.submitExamAnswers(uid, request.getTaskId(), request.getAnswers()));
    }

    private Long extractUid(Authentication auth) {
        Object details = auth.getDetails();
        if (details instanceof io.jsonwebtoken.Claims claims) {
            return ((Number) claims.get("uid")).longValue();
        }
        return 1L;
    }
}
