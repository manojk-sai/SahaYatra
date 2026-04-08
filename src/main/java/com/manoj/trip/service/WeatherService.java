package com.manoj.trip.service;

import com.manoj.trip.dto.response.OpenWeatherRaw;
import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.WeatherSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {
    @Value("${weather.api.url}")
    private String apiUrl;
    @Value("${weather.api.key}")
    private String apiKey;
    private final RestClient restClient;
    private final MongoTemplate mongoTemplate;

    public WeatherService(RestClient.Builder restClientBuilder,
                          MongoTemplate mongoTemplate) {
        this.restClient = restClientBuilder.build();
        this.mongoTemplate = mongoTemplate;
    }

    @Async
    public void fetchAndSaveWeatherSnapshot(String cityName, String stopId) {
        OpenWeatherRaw rawWeather = restClient.get()
                .uri( apiUrl, uriBuilder -> uriBuilder
                        .queryParam("q", cityName+",US")
                        .queryParam("units", "metric")
                        .queryParam("appid", apiKey)
                        .build())
                .retrieve()
                .body(OpenWeatherRaw.class);
        if(rawWeather == null) {
            throw new RuntimeException("Failed to fetch weather data for city: " + cityName);
        }

        WeatherSnapshot weatherSnapshot = WeatherSnapshot.builder()
                .fetchedAt(LocalDateTime.now())
                .description(rawWeather.weather().get(0).description())
                .feelsLike(String.valueOf(Math.round(rawWeather.main().feelsLike())))
                .temp(String.valueOf(Math.round(rawWeather.main().temp())))
                .humidity(String.valueOf(rawWeather.main().humidity()))
                .build();
        Query query = new Query(Criteria.where("stops._id").is(stopId));
        Update update = new Update().set("stops.$.weatherSnapshot", weatherSnapshot);

        mongoTemplate.updateFirst(query, update, "trips");
    }

    public WeatherSnapshot getWeatherSnapshotForStop(String stopId) {
        Query query = new Query(Criteria.where("stops._id").is(stopId));
        Trip trip = mongoTemplate.findOne(query, Trip.class, "trips");
        if(trip == null || trip.getStops() == null) {
            throw new RuntimeException("Trip not found for stopId: " + stopId);
        }
        return trip.getStops().stream()
                .filter(stop -> stop.getId().equals(stopId))
                .map(Stop::getWeatherSnapshot)
                .findFirst()
                .orElse(null);
    }
}
