package com.example.dto;

import com.example.enums.UserRole;

public class LoginResponse {
    private String token;
    private Long userId;
    private UserRole role;

    public LoginResponse(String token, Long userId, UserRole role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public UserRole getRole() { return role; }
}
