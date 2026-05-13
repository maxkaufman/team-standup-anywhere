package com.teampulse.repository;

import com.teampulse.entity.Standup;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StandupRepository extends JpaRepository<Standup, UUID> {

    List<Standup> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    List<Standup> findByTeamIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID teamId, LocalDateTime start, LocalDateTime end);

    List<Standup> findByTeamIdOrderByCreatedAtDesc(UUID teamId);

    @Query("SELECT CAST(s.createdAt AS date), AVG(s.mood), COUNT(s) " +
            "FROM Standup s WHERE s.team.id = :teamId " +
            "AND s.createdAt >= :since " +
            "GROUP BY CAST(s.createdAt AS date) " +
            "ORDER BY CAST(s.createdAt AS date)")
    List<Object[]> getMoodTrendRaw(@Param("teamId") UUID teamId,
                                   @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT s.author.id) FROM Standup s " +
            "WHERE s.team.id = :teamId AND s.createdAt >= :since")
    long countDistinctAuthorsForTeamSince(@Param("teamId") UUID teamId,
                                          @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM Standup s " +
            "WHERE s.team.id = :teamId AND s.blockers IS NOT NULL " +
            "AND s.blockers <> '' AND s.createdAt >= :since")
    long countBlockersForTeamSince(@Param("teamId") UUID teamId,
                                   @Param("since") LocalDateTime since);

    long countByTeamId(UUID teamId);
}
