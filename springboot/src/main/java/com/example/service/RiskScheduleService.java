package com.example.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RiskScheduleService {

    private final TeacherService teacherService;

    public RiskScheduleService(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Scheduled(cron = "0 15 1 * * ?")
    public void runDailyRiskCalculation() {
        teacherService.calculateRisk(LocalDate.now());
    }
}
