package com.manoj.trip.service;

import com.manoj.trip.dto.request.AuthRequest;
import com.manoj.trip.dto.response.AuthResponse;
import com.manoj.trip.dto.request.RegisterRequest;
import com.manoj.trip.exception.UserAlreadyExistsException;
import com.manoj.trip.model.User;
import com.manoj.trip.model.UserProfile;
import com.manoj.trip.repository.UserRepository;
import com.manoj.trip.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    public String signup(RegisterRequest input) {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }
        User user = new User();
        user.setUsername(input.getUsername());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        UserProfile profile = new UserProfile();
        if(input.getCity()!=null){
            profile.setCity(input.getCity());
        }
        if(input.getFullName()!=null){
            profile.setFullName(input.getFullName());
        }
        profile.setZipCode(input.getZipCode());
        user.setProfile(profile);
        userRepository.save(user);

        return "User registered successfully";
    }

    public AuthResponse authenticate(AuthRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        AuthResponse authResponse = new AuthResponse();
        final UserDetails userDetails = userDetailsService.loadUserByUsername(input.getUsername());
        authResponse.setToken(jwtUtil.generateToken(userDetails));
        authResponse.setExpiresBy(jwtUtil.getJwtExpiration());
        return authResponse;
    }
}
