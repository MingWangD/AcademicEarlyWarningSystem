package com.example.api;

import com.example.common.ApiResponse;
import com.example.dto.StudentRequests;
import com.example.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {
    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) { this.teacherService = teacherService; }

    @PostMapping("/task/create")
    public ApiResponse<?> createTask(Authentication auth, @RequestBody StudentRequests.TeacherTaskCreateRequest request) {
        Long uid = extractUid(auth);
        return ApiResponse.ok(Map.of("taskId", teacherService.createTask(request, uid)));
    }

    @GetMapping("/student/activity")
    public ApiResponse<?> activity(@RequestParam(required = false) LocalDate date) {
        return ApiResponse.ok(teacherService.activity(date == null ? LocalDate.now() : date));
    }

    @PostMapping("/risk/calc")
    public ApiResponse<?> calc(@RequestBody StudentRequests.RiskCalcRequest request) {
        LocalDate date = request.getDate() == null ? LocalDate.now() : request.getDate();
        return ApiResponse.ok(teacherService.calculateRisk(date));
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() { return ApiResponse.ok(teacherService.dashboard()); }
    private Long extractUid(Authentication auth) {
        Object details = auth.getDetails();
        if (details instanceof io.jsonwebtoken.Claims claims) {
            return ((Number) claims.get("uid")).longValue();
        }
        return 1L;
    }

}
