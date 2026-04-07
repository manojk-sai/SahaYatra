package com.manoj.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenWeatherRaw(
        List<Weather> weather,
        Main main
) {
    public record Weather(String description) {}
    public record Main(
            double temp,
            @JsonProperty("feels_like") double feelsLike,
            int humidity
    ) {}
}
