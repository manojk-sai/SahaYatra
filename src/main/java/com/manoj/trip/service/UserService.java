package com.manoj.trip.service;

import com.manoj.trip.model.User;
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

    public User update(User user) {
        return userRepository.save(user);
    }
}
