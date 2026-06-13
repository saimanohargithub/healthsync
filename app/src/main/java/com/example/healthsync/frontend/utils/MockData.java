package com.example.healthsync.frontend.utils;

import java.util.ArrayList;
import java.util.List;

public class MockData {
    public static class HealthMetric {
        public String title;
        public String value;
        public int progress;
        public int color;

        public HealthMetric(String title, String value, int progress, int color) {
            this.title = title;
            this.value = value;
            this.progress = progress;
            this.color = color;
        }
    }

    public static List<HealthMetric> getDashboardMetrics() {
        List<HealthMetric> metrics = new ArrayList<>();
        metrics.add(new HealthMetric("Sleep Score", "82/100", 82, 0xFF6366F1));
        metrics.add(new HealthMetric("Stress Level", "Low", 30, 0xFF10B981));
        metrics.add(new HealthMetric("Disease Risk", "Minimal", 15, 0xFF3B82F6));
        return metrics;
    }
}
