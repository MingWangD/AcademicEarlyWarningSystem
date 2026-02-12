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
    public ApiResponse<?> tasks() { return ApiResponse.ok(studentService.getTasks()); }

    @PostMapping("/homework/submit")
    public ApiResponse<?> homework(Authentication auth, @RequestBody StudentRequests.HomeworkSubmitRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.submitHomework(uid, request.getHomeworkId(), request.getContent()));
    }

    @PostMapping("/video/watch")
    public ApiResponse<?> watch(Authentication auth, @RequestBody StudentRequests.VideoWatchRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.watchVideo(uid, request.getVideoId(), request.getWatchTime()));
    }

    @PostMapping("/exam/submit")
    public ApiResponse<?> exam(Authentication auth, @RequestBody StudentRequests.ExamSubmitRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(studentService.submitExam(uid, request.getExamId(), request.getAnswers() == null ? "{}" : request.getAnswers().toString()));
    }
    private Long extractUid(Authentication auth) {
        Object details = auth.getDetails();
        if (details instanceof io.jsonwebtoken.Claims claims) {
            return ((Number) claims.get("uid")).longValue();
        }
        return 1L;
    }

}
