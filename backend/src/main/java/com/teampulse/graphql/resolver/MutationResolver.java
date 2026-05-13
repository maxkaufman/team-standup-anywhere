package com.teampulse.graphql.resolver;

import com.teampulse.entity.Standup;
import com.teampulse.entity.Team;
import com.teampulse.entity.User;
import com.teampulse.graphql.input.ProfileInput;
import com.teampulse.graphql.input.SignUpInput;
import com.teampulse.graphql.input.StandupInput;
import com.teampulse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MutationResolver {

    private final AuthService authService;
    private final TeamService teamService;
    private final StandupService standupService;
    private final UserService userService;
    private final S3Service s3Service;
    private final MetricsCollector metricsCollector;

    // ── Auth ──

    @MutationMapping
    public Map<String, Object> signUp(@Argument SignUpInput input) {
        return authService.signUp(input);
    }

    @MutationMapping
    public Map<String, Object> login(@Argument String email, @Argument String password) {
        return authService.login(email, password);
    }

    @MutationMapping
    public Map<String, Object> refreshToken(@Argument String token) {
        return authService.refreshToken(token);
    }

    // ── Team ──

    @MutationMapping
    public Team createTeam(@Argument String name) {
        User user = authService.getCurrentUser();
        return teamService.createTeam(name, user);
    }

    @MutationMapping
    public Team joinTeam(@Argument String inviteCode) {
        User user = authService.getCurrentUser();
        return teamService.joinTeam(inviteCode, user);
    }

    @MutationMapping
    public boolean removeTeamMember(@Argument UUID userId) {
        User requester = authService.getCurrentUser();
        return teamService.removeTeamMember(userId, requester);
    }

    // ── Standup ──

    @MutationMapping
    public Standup submitStandup(@Argument StandupInput input) {
        User user = authService.getCurrentUser();
        Standup standup = standupService.create(input, user);

        // Record metric in the thread-safe singleton collector
        metricsCollector.recordStandupSubmission(standup.getTeam().getId(), standup.getMood());

        return standup;
    }

    @MutationMapping
    public Standup updateStandup(@Argument UUID id, @Argument StandupInput input) {
        User user = authService.getCurrentUser();
        return standupService.update(id, input, user);
    }

    @MutationMapping
    public boolean deleteStandup(@Argument UUID id) {
        User user = authService.getCurrentUser();
        return standupService.delete(id, user);
    }

    // ── Profile ──

    @MutationMapping
    public User updateProfile(@Argument ProfileInput input) {
        User user = authService.getCurrentUser();
        return userService.updateProfile(user, input);
    }

    @MutationMapping
    public Map<String, String> generateAvatarUploadUrl(@Argument String fileName, @Argument String contentType) {
        return s3Service.generatePresignedUploadUrl(fileName, contentType);
    }
}
