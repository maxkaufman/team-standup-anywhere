package com.teampulse.service;

import com.teampulse.dto.MoodDataPoint;
import com.teampulse.dto.TeamStats;
import com.teampulse.entity.Team;
import com.teampulse.repository.StandupRepository;
import com.teampulse.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StandupRepository standupRepository;
    private final TeamRepository teamRepository;

    public List<MoodDataPoint> getMoodTrend(UUID teamId, int days) {
        LocalDateTime since = LocalDateTime.now(ZoneOffset.UTC).minusDays(days);
        return standupRepository.getMoodTrendRaw(teamId, since).stream()
                .map(row -> new MoodDataPoint(
                        row[0] instanceof java.time.LocalDate ld ? ld
                                : ((java.sql.Date) row[0]).toLocalDate(),
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                        row[2] != null ? ((Number) row[2]).longValue() : 0L))
                .toList();
    }

    public TeamStats getTeamStats(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        LocalDateTime todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
        long totalMembers = team.getMembers().size();
        long submittedToday = standupRepository.countDistinctAuthorsForTeamSince(teamId, todayStart);
        long blockerCount = standupRepository.countBlockersForTeamSince(teamId, todayStart);
        long totalStandups = standupRepository.countByTeamId(teamId);

        // Calculate average mood from recent trend
        List<MoodDataPoint> recentMood = getMoodTrend(teamId, 7);
        double avgMood = recentMood.stream()
                .filter(p -> p.getAvgMood() != null)
                .mapToDouble(MoodDataPoint::getAvgMood)
                .average()
                .orElse(0.0);

        double completionRate = totalMembers > 0 ? (double) submittedToday / totalMembers * 100 : 0;

        return TeamStats.builder()
                .avgMood(Math.round(avgMood * 10.0) / 10.0)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .blockerCount((int) blockerCount)
                .totalStandups(totalStandups)
                .build();
    }
}
