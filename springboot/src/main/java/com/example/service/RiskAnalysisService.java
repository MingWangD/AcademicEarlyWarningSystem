package com.example.service;

import cn.hutool.json.JSONUtil;
import com.example.enums.RiskLevel;
import com.example.mapper.ActivityMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RiskAnalysisService {

    private final ActivityMapper activityMapper;

    public RiskAnalysisService(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    public RiskResult evaluateStudent(Map<String, Object> feature, double[] params) {
        double loginCount = num(feature.get("loginCount"));
        double videoMinutes = Math.max(num(feature.get("videoMinutes")), num(feature.get("rawWatchSeconds")) / 60.0);
        double homeworkSubmitted = num(feature.get("homeworkSubmitted"));
        double avgScore = num(feature.get("avgScore"));
        double examCount = num(feature.get("examCount"));
        double examPassCount = num(feature.get("examPassCount"));

        double homeworkMissRate = examCount > 0 ? Math.max(0, 1 - homeworkSubmitted / Math.max(examCount, 1)) : (homeworkSubmitted == 0 ? 1 : 0);
        double examFailRate = examCount > 0 ? Math.max(0, 1 - examPassCount / examCount) : (avgScore < 60 ? 1 : 0);
        // 与学生任务完成标准保持一致：视频达到 5 分钟视为满足基础学习要求
        double normalizedVideo = Math.min(videoMinutes / 5.0, 1.0);
        double normalizedLogin = Math.min(loginCount / 5.0, 1.0);
        double normalizedScore = Math.min(Math.max(avgScore / 100.0, 0), 1);

        double z = params[0]
                + params[1] * (1 - normalizedScore)
                + params[2] * examFailRate
                + params[3] * homeworkMissRate
                + params[4] * (1 - normalizedVideo)
                + params[5] * (1 - normalizedLogin);

        double score = 1.0 / (1.0 + Math.exp(-z));
        RiskLevel level = score >= 0.7 ? RiskLevel.HIGH : score >= 0.4 ? RiskLevel.MEDIUM : RiskLevel.LOW;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("features", Map.of(
                "avgScore", avgScore,
                "examFailRate", examFailRate,
                "homeworkMissRate", homeworkMissRate,
                "videoMinutes", videoMinutes,
                "loginCount", loginCount
        ));
        detail.put("z", z);

        return new RiskResult(score, level, JSONUtil.toJsonStr(detail));
    }

    public double[] tuneByCrossValidation(LocalDate date) {
        List<Map<String, Object>> rows = activityMapper.historicalFeatures(date.minusDays(30), date.minusDays(1));
        if (rows.isEmpty()) {
            return new double[]{-1.3, 2.4, 2.0, 1.8, 1.1, 0.8};
        }

        double[][] candidates = {
                {-1.5, 2.1, 1.8, 1.6, 1.0, 0.7},
                {-1.3, 2.4, 2.0, 1.8, 1.1, 0.8},
                {-1.1, 2.6, 2.2, 2.0, 1.2, 1.0}
        };

        double bestScore = -1;
        double[] best = candidates[1];
        int folds = 3;
        for (double[] candidate : candidates) {
            double totalF1 = 0;
            for (int f = 0; f < folds; f++) {
                int tp = 0, fp = 0, fn = 0;
                for (int i = 0; i < rows.size(); i++) {
                    if (i % folds != f) continue;
                    Map<String, Object> row = rows.get(i);
                    RiskResult pred = evaluateStudent(row, candidate);
                    boolean predictedRisk = pred.level() != RiskLevel.LOW;
                    boolean actualRisk = deriveLabel(row);
                    if (predictedRisk && actualRisk) tp++;
                    if (predictedRisk && !actualRisk) fp++;
                    if (!predictedRisk && actualRisk) fn++;
                }
                double precision = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
                double recall = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
                double f1 = precision + recall == 0 ? 0 : 2 * precision * recall / (precision + recall);
                totalF1 += f1;
            }
            double avgF1 = totalF1 / folds;
            if (avgF1 > bestScore) {
                bestScore = avgF1;
                best = candidate;
            }
        }
        return best;
    }

    private boolean deriveLabel(Map<String, Object> row) {
        double avgScore = num(row.get("avgScore"));
        double homework = num(row.get("homeworkSubmitted"));
        double video = num(row.get("videoMinutes"));
        return avgScore < 60 || homework < 1 || video < 5;
    }

    private double num(Object n) {
        if (n == null) return 0;
        return n instanceof Number ? ((Number) n).doubleValue() : Double.parseDouble(n.toString());
    }

    public record RiskResult(double score, RiskLevel level, String detailJson) {
    }
}
