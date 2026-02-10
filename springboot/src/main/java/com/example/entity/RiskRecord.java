package com.example.entity;

import com.example.enums.RiskLevel;
import java.time.LocalDate;

public class RiskRecord {
    private Long id;
    private Long studentId;
    private LocalDate calcDate;
    private double riskScore;
    private RiskLevel riskLevel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDate getCalcDate() { return calcDate; }
    public void setCalcDate(LocalDate calcDate) { this.calcDate = calcDate; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
}
