package com.manoj.trip.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class WeatherSnapshot {
    private String temp;
    private String feelsLike;
    private String humidity;
    private String description;
    private LocalDateTime fetchedAt;
}
