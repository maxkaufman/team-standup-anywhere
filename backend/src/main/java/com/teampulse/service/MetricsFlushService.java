package com.teampulse.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Periodically flushes in-memory metrics from MetricsCollector to the database.
 * Runs every 5 minutes to persist aggregated standup data.
 */
@Service
@RequiredArgsConstructor
public class MetricsFlushService {

    private static final Logger log = LoggerFactory.getLogger(MetricsFlushService.class);

    private final MetricsCollector metricsCollector;

    @Scheduled(fixedRate = 300_000) // Every 5 minutes
    public void flushMetrics() {
        Map<UUID, MetricsCollector.TeamMetrics> snapshot = metricsCollector.flushAndReset();

        if (snapshot.isEmpty()) {
            return;
        }

        log.info("Flushing metrics for {} teams", snapshot.size());

        snapshot.forEach((teamId, metrics) -> {
            log.debug("Team {}: {} standups, avg mood {}, {} low mood submissions",
                    teamId, metrics.getStandupCount(), metrics.getAvgMood(), metrics.getLowMoodCount());
            // In production, persist these aggregates to a metrics table in the DB
            // or push to CloudWatch / Prometheus
        });
    }
}
