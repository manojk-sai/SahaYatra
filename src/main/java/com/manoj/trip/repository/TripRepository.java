package com.manoj.trip.repository;

import com.manoj.trip.model.Trip;
import com.manoj.trip.enums.TripStatus;
import com.manoj.trip.enums.TripVisibility;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TripRepository extends MongoRepository<Trip, String> {

    List<Trip> findByMembersUserId(String userId);

    List<Trip> findByStatus(TripStatus status);

    List<Trip> findByVisibility(TripVisibility visibility);

    List<Trip> findByMembersUserIdAndStatus(String userId, TripStatus status);

    @Query("{ 'members.inviteToken': ?0, 'members.active': false }")
    java.util.Optional<Trip> findByPendingInviteToken(String token);
}
