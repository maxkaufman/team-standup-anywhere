package com.teampulse.service;

import com.teampulse.entity.Role;
import com.teampulse.entity.Team;
import com.teampulse.entity.User;
import com.teampulse.exception.TeamNotFoundException;
import com.teampulse.exception.UnauthorizedException;
import com.teampulse.repository.TeamRepository;
import com.teampulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public Team createTeam(String name, User creator) {
        Team team = Team.builder()
                .name(name)
                .inviteCode(generateInviteCode())
                .createdBy(creator)
                .build();

        team = teamRepository.save(team);

        // Make creator the team lead and assign to team
        creator.setTeam(team);
        creator.setRole(Role.LEAD);
        userRepository.save(creator);

        return team;
    }

    @Transactional
    public Team joinTeam(String inviteCode, User user) {
        Team team = teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new TeamNotFoundException("Invalid invite code"));

        if (user.getTeam() != null) {
            throw new IllegalArgumentException("User already belongs to a team");
        }

        user.setTeam(team);
        userRepository.save(user);
        return team;
    }

    public Team getTeamById(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    @Transactional
    public boolean removeTeamMember(UUID userId, User requester) {
        if (requester.getRole() != Role.LEAD && requester.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only team leads can remove members");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (member.getTeam() == null || !member.getTeam().getId().equals(requester.getTeam().getId())) {
            throw new IllegalArgumentException("User is not on your team");
        }

        member.setTeam(null);
        member.setRole(Role.MEMBER);
        userRepository.save(member);
        return true;
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
