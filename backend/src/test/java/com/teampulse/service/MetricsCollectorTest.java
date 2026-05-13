package com.teampulse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the synchronized singleton MetricsCollector.
 * Demonstrates thread safety and singleton guarantee — great interview discussion material.
 */
class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = MetricsCollector.getInstance();
        collector.flushAndReset(); // Clean state between tests
    }

    @Test
    void singletonReturnsSameInstance() {
        MetricsCollector instance1 = MetricsCollector.getInstance();
        MetricsCollector instance2 = MetricsCollector.getInstance();
        assertSame(instance1, instance2, "getInstance() must return the same object");
    }

    @Test
    void recordAndRetrieveMetrics() {
        UUID teamId = UUID.randomUUID();

        collector.recordStandupSubmission(teamId, 4);
        collector.recordStandupSubmission(teamId, 2);
        collector.recordStandupSubmission(teamId, 5);

        MetricsCollector.TeamMetrics metrics = collector.getTeamMetrics(teamId);
        assertEquals(3, metrics.getStandupCount());
        assertEquals(3.67, metrics.getAvgMood(), 0.01);
        assertEquals(1, metrics.getLowMoodCount()); // mood <= 2
    }

    @Test
    void unknownTeamReturnsEmpty() {
        MetricsCollector.TeamMetrics metrics = collector.getTeamMetrics(UUID.randomUUID());
        assertEquals(0, metrics.getStandupCount());
        assertEquals(0.0, metrics.getAvgMood());
    }

    @Test
    void flushAndResetReturnsSnapshotAndClears() {
        UUID teamA = UUID.randomUUID();
        UUID teamB = UUID.randomUUID();

        collector.recordStandupSubmission(teamA, 3);
        collector.recordStandupSubmission(teamB, 5);

        Map<UUID, MetricsCollector.TeamMetrics> snapshot = collector.flushAndReset();

        assertEquals(2, snapshot.size());
        assertTrue(snapshot.containsKey(teamA));
        assertTrue(snapshot.containsKey(teamB));

        // After flush, cache should be empty
        assertEquals(0, collector.getTeamMetrics(teamA).getStandupCount());
        assertEquals(0, collector.getTeamMetrics(teamB).getStandupCount());
    }

    @Test
    void concurrentWritesAreThreadSafe() throws InterruptedException {
        UUID teamId = UUID.randomUUID();
        int threadCount = 100;
        int submissionsPerThread = 50;
        int expectedTotal = threadCount * submissionsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < submissionsPerThread; j++) {
                        collector.recordStandupSubmission(teamId, 3);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        MetricsCollector.TeamMetrics metrics = collector.getTeamMetrics(teamId);
        assertEquals(expectedTotal, metrics.getStandupCount(),
                "All submissions should be recorded — no lost updates from race conditions");
    }

    @Test
    void singletonIsSameAcrossThreads() throws InterruptedException {
        int threadCount = 20;
        MetricsCollector[] instances = new MetricsCollector[threadCount];
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    instances[idx] = MetricsCollector.getInstance();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        for (int i = 1; i < threadCount; i++) {
            assertSame(instances[0], instances[i],
                    "All threads must get the same singleton instance");
        }
    }
}
