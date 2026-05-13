package com.teampulse.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton that aggregates real-time team metrics in-memory.
 *
 * WHY A SINGLETON:
 * - Exactly one instance holds the aggregated state across all request threads
 * - Avoids hitting the database on every dashboard poll
 * - Acts as a write-through cache, periodically flushed to persistent storage
 *
 * WHY SYNCHRONIZED:
 * - Multiple HTTP request threads submit standups concurrently
 * - Each submission updates shared counters (mood sums, standup counts)
 * - Without synchronization, we'd get race conditions on counter updates
 *
 * INTERVIEW TALKING POINTS:
 * - Double-checked locking avoids sync overhead after initialization
 * - volatile prevents instruction reordering (partial object visibility)
 * - ConcurrentHashMap for key-level concurrency, synchronized for bulk ops
 * - Spring beans are singletons by default, but this pattern demonstrates
 *   JVM-level thread safety — useful if used outside Spring (e.g., Lambda)
 * - Trade-off: in-memory cache lost on restart → discuss Redis alternative at scale
 */
public class MetricsCollector {

    // Volatile ensures visibility of the instance reference across threads
    private static volatile MetricsCollector instance;

    // Team ID → current aggregated metrics (ConcurrentHashMap for key-level safety)
    private final ConcurrentHashMap<UUID, TeamMetrics> metricsCache = new ConcurrentHashMap<>();

    // Private constructor — enforces singleton
    private MetricsCollector() {}

    /**
     * Double-checked locking: thread-safe lazy initialization.
     * First check avoids lock overhead when instance already exists.
     * Second check (inside synchronized) prevents duplicate creation.
     */
    public static MetricsCollector getInstance() {
        if (instance == null) {                       // 1st check — no lock
            synchronized (MetricsCollector.class) {    // Acquire lock
                if (instance == null) {               // 2nd check — with lock
                    instance = new MetricsCollector();
                }
            }
        }
        return instance;
    }

    /**
     * Called on every standup submission. Thread-safe via ConcurrentHashMap.compute().
     */
    public void recordStandupSubmission(UUID teamId, int mood) {
        metricsCache.compute(teamId, (key, existing) -> {
            if (existing == null) {
                return new TeamMetrics(1, mood, (mood <= 2) ? 1 : 0);
            }
            existing.addStandup(mood);
            return existing;
        });
    }

    /**
     * Fast read from cache — called by dashboard queries instead of hitting DB.
     */
    public TeamMetrics getTeamMetrics(UUID teamId) {
        return metricsCache.getOrDefault(teamId, TeamMetrics.empty());
    }

    /**
     * Atomically snapshot and reset all metrics.
     * Called by a @Scheduled flush job to persist aggregates to the database.
     * Synchronized because we need the snapshot + clear to be atomic.
     */
    public synchronized Map<UUID, TeamMetrics> flushAndReset() {
        Map<UUID, TeamMetrics> snapshot = new HashMap<>(metricsCache);
        metricsCache.clear();
        return snapshot;
    }

    /**
     * Inner class holding per-team aggregated metrics.
     * Methods are synchronized because multiple threads call addStandup concurrently.
     */
    public static class TeamMetrics {
        private int standupCount;
        private double moodSum;
        private int lowMoodCount;

        public TeamMetrics(int standupCount, double moodSum, int lowMoodCount) {
            this.standupCount = standupCount;
            this.moodSum = moodSum;
            this.lowMoodCount = lowMoodCount;
        }

        public synchronized void addStandup(int mood) {
            standupCount++;
            moodSum += mood;
            if (mood <= 2) {
                lowMoodCount++;
            }
        }

        public synchronized double getAvgMood() {
            return standupCount == 0 ? 0.0 : moodSum / standupCount;
        }

        public synchronized int getStandupCount() {
            return standupCount;
        }

        public synchronized int getLowMoodCount() {
            return lowMoodCount;
        }

        public static TeamMetrics empty() {
            return new TeamMetrics(0, 0, 0);
        }
    }
}
