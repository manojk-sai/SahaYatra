package com.manoj.trip.controller;

import com.manoj.trip.dto.request.UpdateProfileRequest;
import com.manoj.trip.model.User;
import com.manoj.trip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations for managing users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get user details", description = "Returns a single user object based on the authenticated user")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.findByUsername(currentUser.getUsername()));
    }

    @PostMapping("/me")
    @Operation(summary = "Update user details", description = "Returns a single user object based on the updated user object")
    public ResponseEntity<User> updateAuthenticatedUser(
            @Parameter(description = "User profile object with updated values") @RequestBody UpdateProfileRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(userService.update(currentUser.getUsername(), request));
    }
}
