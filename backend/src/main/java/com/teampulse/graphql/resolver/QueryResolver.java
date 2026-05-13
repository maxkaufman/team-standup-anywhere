package com.teampulse.graphql.resolver;

import com.teampulse.dto.MoodDataPoint;
import com.teampulse.dto.TeamStats;
import com.teampulse.entity.Standup;
import com.teampulse.entity.Team;
import com.teampulse.entity.User;
import com.teampulse.service.AnalyticsService;
import com.teampulse.service.AuthService;
import com.teampulse.service.StandupService;
import com.teampulse.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class QueryResolver {

    private final AuthService authService;
    private final TeamService teamService;
    private final StandupService standupService;
    private final AnalyticsService analyticsService;

    @QueryMapping
    public User me() {
        return authService.getCurrentUser();
    }

    @QueryMapping
    public Team team(@Argument UUID id) {
        return teamService.getTeamById(id);
    }

    @QueryMapping
    public Team myTeam() {
        User user = authService.getCurrentUser();
        return user.getTeam();
    }

    @QueryMapping
    public List<Standup> standups(@Argument UUID teamId, @Argument String date,
                                   @Argument int limit, @Argument int offset) {
        return standupService.getStandupsByTeam(teamId, date, limit, offset);
    }

    @QueryMapping
    public List<Standup> myStandups(@Argument int limit, @Argument int offset) {
        User user = authService.getCurrentUser();
        return standupService.getMyStandups(user.getId(), limit, offset);
    }

    @QueryMapping
    public Standup todayStandup() {
        User user = authService.getCurrentUser();
        return standupService.getTodayStandup(user);
    }

    @QueryMapping
    public List<MoodDataPoint> teamMoodTrend(@Argument UUID teamId, @Argument int days) {
        return analyticsService.getMoodTrend(teamId, days);
    }

    @QueryMapping
    public TeamStats teamStats(@Argument UUID teamId) {
        return analyticsService.getTeamStats(teamId);
    }
}
