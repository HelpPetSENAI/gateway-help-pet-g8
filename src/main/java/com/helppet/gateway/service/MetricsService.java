package com.helppet.gateway.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    // Simple in-memory storage for demonstration purposes. In a real app, use Redis or a Database.
    private final Map<LocalDate, DailyMetrics> metricsStore = new ConcurrentHashMap<>();

    public void incrementRequestCount() {
        LocalDate today = LocalDate.now();
        metricsStore.computeIfAbsent(today, k -> new DailyMetrics(today)).incrementRequests();
    }

    public void incrementResponseCount() {
        LocalDate today = LocalDate.now();
        metricsStore.computeIfAbsent(today, k -> new DailyMetrics(today)).incrementResponses();
    }

    public List<DailyMetrics> getLast7DaysMetrics() {
        LocalDate today = LocalDate.now();
        List<DailyMetrics> result = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.add(metricsStore.getOrDefault(date, new DailyMetrics(date)));
        }
        
        return result;
    }

    public static class DailyMetrics {
        private final String date;
        private final AtomicLong requests = new AtomicLong(0);
        private final AtomicLong responses = new AtomicLong(0);

        public DailyMetrics(LocalDate date) {
            this.date = date.toString();
        }

        public void incrementRequests() {
            requests.incrementAndGet();
        }

        public void incrementResponses() {
            responses.incrementAndGet();
        }

        public String getDate() { return date; }
        public long getRequests() { return requests.get(); }
        public long getResponses() { return responses.get(); }
    }
}
