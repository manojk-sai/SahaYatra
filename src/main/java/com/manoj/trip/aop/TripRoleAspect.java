package com.manoj.trip.aop;

import com.manoj.trip.enums.MemberRole;
import com.manoj.trip.model.Trip;
import com.manoj.trip.model.TripMembership;
import com.manoj.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class TripRoleAspect {
    private final TripRepository tripRepository;

    private static final MemberRole[] hierarchy = {
            MemberRole.VIEWER,
            MemberRole.CONTRIBUTOR,
            MemberRole.ORGANIZER
    };

    @Before("@annotation(com.manoj.trip.aop.RequiresTripRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature sign = (MethodSignature) joinPoint.getSignature();
        Method method = sign.getMethod();
        MemberRole requiredRole = method.getAnnotation(RequiresTripRole.class).value();

        Object[] args = joinPoint.getArgs();
        if(args.length < 2) {
            throw new IllegalStateException(
                    "@RequiresTripRole methods must have at least 2 parameters (tripId and userId)"
            );
        }

        String tripId = (String) args[0];
        String userId = (String) args[1];

        Trip trip = tripRepository.findById(tripId).orElseThrow(()-> new RuntimeException("Trip not found: "+tripId));

        TripMembership membership = trip.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User " + userId + " is not an active member of the trip " + tripId));

        if(!hasRequiredRole(membership.getRole(), requiredRole)) {
            throw new RuntimeException("User " + userId + " does not have the required role " + requiredRole + " for trip " + tripId + " but has role " + membership.getRole());
        }
    }

    //returns true if the user's role meets or exceeds the required role in the hierarchy
    private boolean hasRequiredRole(MemberRole role, MemberRole requiredRole) {
        return rankOf(role) >= rankOf(requiredRole);
    }

    private int rankOf(MemberRole role) {
        for(int i=0; i<hierarchy.length; i++) {
            if(hierarchy[i] == role) return i;
        }
        return -1;
    }
}
