package com.teampulse.graphql.resolver;

import com.teampulse.dto.MoodDataPoint;
import com.teampulse.dto.TeamStats;
import com.teampulse.entity.Standup;
import com.teampulse.entity.Team;
import com.teampulse.entity.User;
import com.teampulse.service.AnalyticsService;
import com.teampulse.service.StandupService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TeamResolver {

    private final StandupService standupService;
    private final AnalyticsService analyticsService;

    @SchemaMapping(typeName = "Team", field = "members")
    public List<User> members(Team team) {
        return team.getMembers();
    }

    @SchemaMapping(typeName = "Team", field = "standups")
    public List<Standup> standups(Team team, @Argument String date) {
        return standupService.getStandupsByTeam(team.getId(), date, 50, 0);
    }

    @SchemaMapping(typeName = "Team", field = "moodTrend")
    public List<MoodDataPoint> moodTrend(Team team, @Argument int days) {
        return analyticsService.getMoodTrend(team.getId(), days);
    }

    @SchemaMapping(typeName = "Team", field = "stats")
    public TeamStats stats(Team team) {
        return analyticsService.getTeamStats(team.getId());
    }

    @SchemaMapping(typeName = "Team", field = "createdAt")
    public String createdAt(Team team) {
        return team.getCreatedAt() != null ? team.getCreatedAt().toString() : null;
    }
}
