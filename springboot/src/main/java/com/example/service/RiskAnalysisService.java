package com.example.service;

import cn.hutool.json.JSONUtil;
import com.example.enums.RiskLevel;
import com.example.mapper.ActivityMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RiskAnalysisService {

    private static final double TRAIN_LR = 0.08;
    private static final int TRAIN_EPOCHS = 500;
    private static final double L2_LAMBDA = 1e-3;

    private final ActivityMapper activityMapper;

    public RiskAnalysisService(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    public RiskResult evaluateStudent(Map<String, Object> feature, ModelProfile profile) {
        FeatureVector v = vectorize(feature);
        double[] x = normalize(v.toArray(), profile.means(), profile.stds());
        double z = dot(profile.weights(), x) + profile.bias();
        double score = sigmoid(z);
        RiskLevel level = score >= profile.highThreshold() ? RiskLevel.HIGH
                : score >= profile.mediumThreshold() ? RiskLevel.MEDIUM
                : RiskLevel.LOW;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("features", Map.of(
                "avgScore", v.avgScore,
                "examFailRate", v.examFailRate,
                "homeworkMissRate", v.homeworkMissRate,
                "videoMinutes", v.videoMinutes,
                "loginCount", v.loginCount
        ));
        detail.put("z", z);
        detail.put("threshold", Map.of(
                "medium", profile.mediumThreshold(),
                "high", profile.highThreshold()
        ));

        return new RiskResult(score, level, JSONUtil.toJsonStr(detail));
    }

    public ModelProfile trainModel(LocalDate date) {
        List<Map<String, Object>> rows = activityMapper.historicalFeatures(date.minusDays(60), date.minusDays(1));
        if (rows.size() < 20) {
            return defaultProfile();
        }

        List<TrainSample> samples = buildSamples(rows);
        if (samples.size() < 20) {
            return defaultProfile();
        }

        double[] means = calcMeans(samples);
        double[] stds = calcStds(samples, means);
        double[] weights = new double[5];
        double bias = 0;

        for (int epoch = 0; epoch < TRAIN_EPOCHS; epoch++) {
            double[] gradW = new double[weights.length];
            double gradB = 0;
            for (TrainSample sample : samples) {
                double[] x = normalize(sample.x(), means, stds);
                double pred = sigmoid(dot(weights, x) + bias);
                double err = pred - sample.y();
                for (int i = 0; i < gradW.length; i++) {
                    gradW[i] += err * x[i];
                }
                gradB += err;
            }
            double n = samples.size();
            for (int i = 0; i < weights.length; i++) {
                gradW[i] = gradW[i] / n + L2_LAMBDA * weights[i];
                weights[i] -= TRAIN_LR * gradW[i];
            }
            bias -= TRAIN_LR * (gradB / n);
        }

        List<Double> probs = new ArrayList<>(samples.size());
        List<Integer> labels = new ArrayList<>(samples.size());
        for (TrainSample sample : samples) {
            double p = sigmoid(dot(weights, normalize(sample.x(), means, stds)) + bias);
            probs.add(p);
            labels.add(sample.y());
        }

        double mediumThreshold = findBestThreshold(probs, labels, 0.30, 0.85);
        double highThreshold = findBestThreshold(probs, labels, Math.max(0.55, mediumThreshold + 0.10), 0.95);
        if (highThreshold <= mediumThreshold) {
            highThreshold = Math.min(0.90, mediumThreshold + 0.20);
        }

        Metrics metrics = evaluate(probs, labels, mediumThreshold);
        return new ModelProfile(weights, bias, means, stds, mediumThreshold, highThreshold, metrics);
    }

    private List<TrainSample> buildSamples(List<Map<String, Object>> rows) {
        Map<String, Map<String, Object>> byKey = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = row.get("studentId") + "#" + row.get("activityDate");
            byKey.put(key, row);
        }

        List<TrainSample> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long sid = ((Number) row.get("studentId")).longValue();
            LocalDate day = parseDate(row.get("activityDate"));
            if (day == null) continue;
            Map<String, Object> nextDay = byKey.get(sid + "#" + day.plusDays(1));
            if (nextDay == null) continue;

            FeatureVector fv = vectorize(row);
            int label = deriveLabel(nextDay);
            out.add(new TrainSample(fv.toArray(), label));
        }
        return out;
    }

    private int deriveLabel(Map<String, Object> row) {
        double avgScore = num(row.get("avgScore"));
        double homework = num(row.get("homeworkSubmitted"));
        double video = Math.max(num(row.get("videoMinutes")), num(row.get("rawWatchSeconds")) / 60.0);
        double exams = num(row.get("examCount"));
        double examPass = num(row.get("examPassCount"));
        double examFailRate = exams > 0 ? Math.max(0, 1 - examPass / exams) : (avgScore < 60 ? 1 : 0);

        int riskSignals = 0;
        if (avgScore < 60) riskSignals++;
        if (homework < 1) riskSignals++;
        if (video < 5) riskSignals++;
        if (examFailRate >= 0.5) riskSignals++;
        return riskSignals >= 2 ? 1 : 0;
    }

    private FeatureVector vectorize(Map<String, Object> feature) {
        double loginCount = Math.max(num(feature.get("loginCount")), 0);
        double videoMinutes = Math.max(num(feature.get("videoMinutes")), num(feature.get("rawWatchSeconds")) / 60.0);
        double homeworkSubmitted = Math.max(num(feature.get("homeworkSubmitted")), 0);
        double avgScore = Math.min(Math.max(num(feature.get("avgScore")), 0), 100);
        double examCount = Math.max(num(feature.get("examCount")), 0);
        double examPassCount = Math.max(num(feature.get("examPassCount")), 0);

        double homeworkMissRate = (homeworkSubmitted + 1) <= 0 ? 1 : Math.max(0, 1 - (homeworkSubmitted / (homeworkSubmitted + 1.0)));
        double examFailRate = examCount > 0 ? Math.max(0, 1 - examPassCount / examCount) : (avgScore < 60 ? 1 : 0);
        double normalizedVideoGap = 1 - Math.min(videoMinutes / 20.0, 1.0);
        double normalizedLoginGap = 1 - Math.min(loginCount / 4.0, 1.0);
        double scoreGap = 1 - avgScore / 100.0;

        return new FeatureVector(scoreGap, examFailRate, homeworkMissRate, normalizedVideoGap, normalizedLoginGap,
                avgScore, examFailRate, homeworkMissRate, videoMinutes, loginCount);
    }

    private double[] calcMeans(List<TrainSample> samples) {
        double[] means = new double[5];
        for (TrainSample s : samples) {
            for (int i = 0; i < means.length; i++) {
                means[i] += s.x()[i];
            }
        }
        for (int i = 0; i < means.length; i++) {
            means[i] /= samples.size();
        }
        return means;
    }

    private double[] calcStds(List<TrainSample> samples, double[] means) {
        double[] stds = new double[5];
        for (TrainSample s : samples) {
            for (int i = 0; i < stds.length; i++) {
                double diff = s.x()[i] - means[i];
                stds[i] += diff * diff;
            }
        }
        for (int i = 0; i < stds.length; i++) {
            stds[i] = Math.sqrt(stds[i] / samples.size());
            if (stds[i] < 1e-6) stds[i] = 1;
        }
        return stds;
    }

    private double[] normalize(double[] x, double[] means, double[] stds) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = (x[i] - means[i]) / stds[i];
        }
        return out;
    }

    private double findBestThreshold(List<Double> probs, List<Integer> labels, double min, double max) {
        double bestT = min;
        double bestF1 = -1;
        for (double t = min; t <= max; t += 0.01) {
            Metrics m = evaluate(probs, labels, t);
            if (m.f1 > bestF1 || (Math.abs(m.f1 - bestF1) < 1e-9 && Math.abs(t - 0.5) < Math.abs(bestT - 0.5))) {
                bestF1 = m.f1;
                bestT = t;
            }
        }
        return Math.round(bestT * 100.0) / 100.0;
    }

    private Metrics evaluate(List<Double> probs, List<Integer> labels, double threshold) {
        int tp = 0, fp = 0, fn = 0, tn = 0;
        for (int i = 0; i < probs.size(); i++) {
            boolean pred = probs.get(i) >= threshold;
            boolean real = labels.get(i) == 1;
            if (pred && real) tp++;
            else if (pred) fp++;
            else if (real) fn++;
            else tn++;
        }
        double precision = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
        double recall = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
        double f1 = precision + recall == 0 ? 0 : 2 * precision * recall / (precision + recall);
        double accuracy = (double) (tp + tn) / Math.max(tp + tn + fp + fn, 1);
        return new Metrics(precision, recall, f1, accuracy);
    }

    private ModelProfile defaultProfile() {
        return new ModelProfile(
                new double[]{2.3, 1.8, 1.1, 1.2, 0.9},
                -1.2,
                new double[]{0, 0, 0, 0, 0},
                new double[]{1, 1, 1, 1, 1},
                0.45,
                0.72,
                new Metrics(0, 0, 0, 0)
        );
    }

    private LocalDate parseDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate date) return date;
        return LocalDate.parse(String.valueOf(v));
    }

    private double sigmoid(double z) {
        if (z > 35) return 1.0;
        if (z < -35) return 0.0;
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private double dot(double[] w, double[] x) {
        double s = 0;
        for (int i = 0; i < w.length; i++) s += w[i] * x[i];
        return s;
    }

    private double num(Object n) {
        if (n == null) return 0;
        return n instanceof Number ? ((Number) n).doubleValue() : Double.parseDouble(n.toString());
    }

    public record RiskResult(double score, RiskLevel level, String detailJson) {
    }

    public record ModelProfile(double[] weights, double bias, double[] means, double[] stds,
                               double mediumThreshold, double highThreshold, Metrics metrics) {
    }

    public record Metrics(double precision, double recall, double f1, double accuracy) {
    }

    private record TrainSample(double[] x, int y) {
    }

    private record FeatureVector(double scoreGap, double examFailRate, double homeworkMissRate,
                                 double normalizedVideoGap, double normalizedLoginGap,
                                 double avgScore, double examFailRateDisplay,
                                 double homeworkMissRateDisplay, double videoMinutes,
                                 double loginCount) {
        private double[] toArray() {
            return new double[]{scoreGap, examFailRate, homeworkMissRate, normalizedVideoGap, normalizedLoginGap};
        }
    }
}
