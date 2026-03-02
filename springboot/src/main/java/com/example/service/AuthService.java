package com.example.service;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.entity.AppUser;
import com.example.enums.UserRole;
import com.example.mapper.ActivityMapper;
import com.example.mapper.UserMapper;
import com.example.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ActivityMapper activityMapper;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, ActivityMapper activityMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.activityMapper = activityMapper;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = userMapper.findByUsername(request.getUsername());
        if (user == null || user.getRole() != request.getRole() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名/密码错误或角色不匹配");
        }
        if (user.getRole() == UserRole.STUDENT) {
            activityMapper.upsertDailyLogin(user.getId());
        }
        String token = tokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getRole());
    }
}
