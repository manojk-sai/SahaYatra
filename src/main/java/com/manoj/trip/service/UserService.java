package com.manoj.trip.service;

import com.manoj.trip.dto.request.UpdateProfileRequest;
import com.manoj.trip.model.User;
import com.manoj.trip.model.UserProfile;
import com.manoj.trip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User update(String username, UpdateProfileRequest request) {
        User existingUser = findByUsername(username);
        UserProfile profile = existingUser.getProfile();
        if (profile == null) {
            profile = new UserProfile();
        }

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getZipCode() != null) {
            profile.setZipCode(request.getZipCode());
        }

        existingUser.setProfile(profile);
        return userRepository.save(existingUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
