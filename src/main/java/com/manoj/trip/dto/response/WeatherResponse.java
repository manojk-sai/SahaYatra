package com.manoj.trip.dto.response;

import java.time.LocalDateTime;

public record WeatherResponse(
        String temp,
        String feelsLike,
        String humidity,
        String description,
        LocalDateTime fetchedAt
) {}