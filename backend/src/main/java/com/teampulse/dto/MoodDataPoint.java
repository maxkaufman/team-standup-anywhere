package com.teampulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodDataPoint {
    private LocalDate date;
    private Double avgMood;
    private Long count;
}
