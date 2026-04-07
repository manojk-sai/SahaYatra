package com.manoj.trip.service;

import com.manoj.trip.dto.response.OpenWeatherRaw;
import com.manoj.trip.dto.response.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

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
        WeatherResponse weatherResponse = new WeatherResponse(
                String.valueOf(Math.round(rawWeather.main().temp())),
                String.valueOf(Math.round(rawWeather.main().feelsLike())),
                String.valueOf(rawWeather.main().humidity()),
                rawWeather.weather().get(0).description(),
                LocalDateTime.now());
        Query query = new Query(Criteria.where("stops._id").is(stopId));
        Update update = new Update().set("stops.$.weatherSnapshot", weatherResponse);

        mongoTemplate.updateFirst(query, update, "trips");
    }
}
