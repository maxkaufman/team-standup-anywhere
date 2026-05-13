package com.teampulse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "standups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Standup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String yesterday;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String today;

    @Column(columnDefinition = "TEXT")
    private String blockers;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private int mood;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
