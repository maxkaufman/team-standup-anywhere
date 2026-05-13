package com.teampulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamStats {
    private double avgMood;
    private double completionRate;
    private int blockerCount;
    private long totalStandups;
}
