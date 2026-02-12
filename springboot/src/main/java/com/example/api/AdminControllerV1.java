package com.example.api;

import com.example.common.ApiResponse;
import com.example.entity.AppUser;
import com.example.mapper.ActivityMapper;
import com.example.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminControllerV1 {
    private final AdminUserService adminUserService;
    private final ActivityMapper activityMapper;

    public AdminControllerV1(AdminUserService adminUserService, ActivityMapper activityMapper) {
        this.adminUserService = adminUserService;
        this.activityMapper = activityMapper;
    }

    @GetMapping("/user")
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(adminUserService.list(pageNum, pageSize));
    }

    @PostMapping("/user")
    public ApiResponse<?> create(@RequestBody AppUser user) { return ApiResponse.ok(adminUserService.create(user)); }

    @PutMapping("/user")
    public ApiResponse<?> update(@RequestBody AppUser user) { return ApiResponse.ok(adminUserService.update(user)); }

    @DeleteMapping("/user/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() { return ApiResponse.ok(adminUserService.dashboard(activityMapper)); }
}
