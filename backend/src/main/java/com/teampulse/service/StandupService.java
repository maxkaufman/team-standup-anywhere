package com.teampulse.service;

import com.teampulse.entity.Standup;
import com.teampulse.entity.User;
import com.teampulse.exception.UnauthorizedException;
import com.teampulse.graphql.input.StandupInput;
import com.teampulse.repository.StandupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StandupService {

    private final StandupRepository standupRepository;

    @Transactional
    public Standup create(StandupInput input, User author) {
        if (author.getTeam() == null) {
            throw new IllegalArgumentException("You must belong to a team to submit a standup");
        }

        Standup standup = Standup.builder()
                .author(author)
                .team(author.getTeam())
                .yesterday(input.getYesterday())
                .today(input.getToday())
                .blockers(input.getBlockers())
                .mood(input.getMood())
                .build();

        return standupRepository.save(standup);
    }

    @Transactional
    public Standup update(UUID id, StandupInput input, User requester) {
        Standup standup = standupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standup not found"));

        if (!standup.getAuthor().getId().equals(requester.getId())) {
            throw new UnauthorizedException("You can only edit your own standups");
        }

        standup.setYesterday(input.getYesterday());
        standup.setToday(input.getToday());
        standup.setBlockers(input.getBlockers());
        standup.setMood(input.getMood());

        return standupRepository.save(standup);
    }

    @Transactional
    public boolean delete(UUID id, User requester) {
        Standup standup = standupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standup not found"));

        if (!standup.getAuthor().getId().equals(requester.getId())) {
            throw new UnauthorizedException("You can only delete your own standups");
        }

        standupRepository.delete(standup);
        return true;
    }

    public List<Standup> getStandupsByTeam(UUID teamId, String date, int limit, int offset) {
        if (date != null) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime start = localDate.atStartOfDay();
            LocalDateTime end = localDate.atTime(LocalTime.MAX);
            return standupRepository.findByTeamIdAndCreatedAtBetweenOrderByCreatedAtDesc(teamId, start, end);
        }
        return standupRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }

    public List<Standup> getMyStandups(UUID authorId, int limit, int offset) {
        int page = limit > 0 ? offset / limit : 0;
        return standupRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, PageRequest.of(page, Math.max(limit, 1)));
    }

    public Standup getTodayStandup(User user) {
        LocalDateTime start = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
        LocalDateTime end = LocalDate.now(ZoneOffset.UTC).atTime(LocalTime.MAX);

        return standupRepository.findByTeamIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        user.getTeam().getId(), start, end)
                .stream()
                .filter(s -> s.getAuthor().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
    }
}
