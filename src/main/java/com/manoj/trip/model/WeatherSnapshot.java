package com.manoj.trip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSnapshot {
    private String temp;
    private String feelsLike;
    private String humidity;
    private String description;
    private LocalDateTime fetchedAt;
}
