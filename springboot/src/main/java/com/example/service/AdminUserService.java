package com.example.service;

import com.example.mapper.ActivityMapper;
import com.example.entity.AppUser;
import com.example.enums.RiskLevel;
import com.example.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> list(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<AppUser> records = userMapper.findPage(offset, pageSize);
        long total = userMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    public AppUser create(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRiskLevel() == null) {
            user.setRiskLevel(RiskLevel.LOW);
        }
        userMapper.insert(user);
        return user;
    }

    public AppUser update(AppUser user) {
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public Map<String, Object> dashboard(ActivityMapper activityMapper) {
        Map<String, Object> map = new HashMap<>();
        map.put("riskDistribution", activityMapper.latestRiskDistribution());
        map.put("riskTrend", activityMapper.riskTrend());
        map.put("recentWarnings", activityMapper.recentWarnings());
        map.put("totalUsers", userMapper.countAll());
        return map;
    }
}
