package com.manoj.trip.service;

import com.manoj.trip.model.Stop;
import com.manoj.trip.model.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class WeatherScheduler {
    private final WeatherService weatherService;
    private final MongoTemplate mongoTemplate;

    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void refreshWeatherData() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        // Query: Status is PLANNING AND (any stop has weather older than 24h OR weather is null)
        Query query = new Query(
                Criteria.where("status").is("PLANNING")
                        .orOperator(
                                Criteria.where("stops.weatherSnapshot.fetchedAt").lt(twentyFourHoursAgo),
                                Criteria.where("stops.weatherSnapshot").exists(false)
                        )
        );

        List<Trip> tripsToUpdate = mongoTemplate.find(query, Trip.class);

        for (Trip trip : tripsToUpdate) {
            for (Stop stop : trip.getStops()) {
                // Check if this specific stop needs an update
                if (stop.getWeatherSnapshot() == null ||
                        stop.getWeatherSnapshot().getFetchedAt().isBefore(twentyFourHoursAgo)) {
                        weatherService.fetchAndSaveWeatherSnapshot(
                            stop.getId(),
                            stop.getLocation()
                    );
                }
            }
        }
    }
}
