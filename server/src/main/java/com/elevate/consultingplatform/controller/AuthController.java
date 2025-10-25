package com.elevate.consultingplatform.controller;

import com.elevate.consultingplatform.dto.auth.AdminRegistrationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationResponse;
import com.elevate.consultingplatform.dto.auth.RegisterRequest;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.exception.EmailAlreadyExistsException;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.security.JwtService;
import com.elevate.consultingplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/admin/register")
    @Operation(summary = "Register a new admin user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest request
    ) {
        RegisterRequest userRequest = request.getUserDetails();
        
        // Check if email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }
        
        // Create new user with specified role
        var user = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(request.getRole())
                .isEmailVerified(true)  // Skip email verification for admin-created accounts
                .build();
                
        var savedUser = userRepository.save(user);
        
        // Generate JWT tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .build());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user's email using verification token")
    public ResponseEntity<Boolean> verifyEmail(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> forgotPassword(
            @RequestParam String email
    ) {
        authService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<Boolean> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user details")
    public ResponseEntity<AuthenticationResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
