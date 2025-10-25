package com.elevate.consultingplatform.service.impl;

import com.elevate.consultingplatform.entity.EmailVerificationToken;
import com.elevate.consultingplatform.entity.PasswordResetToken;
import com.elevate.consultingplatform.exception.TokenExpiredException;
import com.elevate.consultingplatform.exception.UserNotFoundException;
import com.elevate.consultingplatform.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import com.elevate.consultingplatform.service.EmailService;
import com.elevate.consultingplatform.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.elevate.consultingplatform.dto.auth.AuthenticationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationResponse;
import com.elevate.consultingplatform.dto.auth.RegisterRequest;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.exception.EmailAlreadyExistsException;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.EmailVerificationTokenRepository;
import com.elevate.consultingplatform.repository.PasswordResetTokenRepository;
import com.elevate.consultingplatform.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserDetailsService userDetailsService;
    
    @Value("${app.security.verification-token.expiration-minutes}")
    private int verificationTokenExpirationMinutes;
    
    @Value("${app.security.password-reset-token.expiration-minutes}")
    private int passwordResetTokenExpirationMinutes;
    
    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;
    
    @EventListener(ApplicationReadyEvent.class)
    public void createAdminUser() {
        try {
            log.info("Checking admin user with email: {}", adminEmail);
            var existingAdmin = userRepository.findByEmail(adminEmail);
            
            if (existingAdmin.isEmpty()) {
                log.info("Creating new admin user...");
                var adminUser = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        //.isActive(true)  // This is required for login
                        .isEmailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                adminUser = userRepository.save(adminUser);
                log.info("Admin user created successfully with ID: {}", adminUser.getId());
                log.info("Admin user created with email: {}", adminEmail);
                log.info("Admin user password: {}", adminPassword); // Remove this in production
            } else {
                // Update admin password if it was changed in properties
                userRepository.findByEmail(adminEmail).ifPresent(admin -> {
                    if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                        admin.setPassword(passwordEncoder.encode(adminPassword));
                        userRepository.save(admin);
                        log.info("Admin password updated for email: {}", adminEmail);
                    }
                });
                log.info("Admin user already exists with email: {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("Error creating/updating admin user: {}", e.getMessage(), e);
        }
    }
    

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Starting registration for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already in use");
        }

        try {
            // Create new user with default CLIENT role
            var user = User.builder()
                    .firstName(request.getFirstName().trim())
                    .lastName(request.getLastName().trim())
                    .email(request.getEmail().toLowerCase().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.CLIENT)
                    .isEmailVerified(false)
                    .isActive(true)  // Set user as active by default
                    .build();

            log.debug("User object created: {}", user);

            // Save user
            var savedUser = userRepository.save(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());
            
            // Generate verification token
            String verificationToken = UUID.randomUUID().toString();
            // TODO: Save verification token with user and send verification email
            
            // Generate JWT tokens
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole())
                    .fullName(user.getFullName())
                    .isEmailVerified(user.isEmailVerified())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage());
        }
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authentication attempt for email: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
            
            // Get user from database
            var user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", request.getEmail());
                        return new UsernameNotFoundException("User not found");
                    });
            
            if (!user.isActive()) {
                log.warn("Login failed - User account is not active: {}", request.getEmail());
                throw new RuntimeException("User account is not active");
            }
            
            if (!user.isEmailVerified()) {
                log.warn("Login failed - Email not verified: {}", request.getEmail());
                // You might want to handle this differently, e.g., by sending a new verification email
                throw new RuntimeException("Email not verified. Please check your email for verification link.");
            }
            
            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate JWT tokens
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            
            log.info("User authenticated successfully: {}", request.getEmail());
            
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole())
                    .fullName(user.getFullName())
                    .isEmailVerified(user.isEmailVerified())
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
    

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        String refreshToken;
        final String userEmail;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }
        
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        
        if (userEmail != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var accessToken = jwtService.generateToken(userDetails);
                var newRefreshToken = jwtService.generateRefreshToken(userDetails);
                
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(
                    response.getOutputStream(),
                    AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .email(userEmail)
                        .role(Role.valueOf(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth) // Remove ROLE_ prefix if present
                            .findFirst()
                            .orElse("USER")))
                        .build()
                );
                return;
            }
        }
        
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid refresh token");
    }

    @Override
    @Transactional
    public void verifyEmail(String verificationToken) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(verificationToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (token.isUsed()) {
            throw new InvalidTokenException("Token already used");
        }

        if (token.isExpired()) {
            throw new ExpiredJwtException(null, null, "Verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark token as used
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Invalidate any existing tokens
        emailVerificationTokenRepository.findByUser(user).ifPresent(token -> {
            token.setUsed(true);
            emailVerificationTokenRepository.save(token);
        });

        // Generate and save new verification token
        String newToken = UUID.randomUUID().toString();
        saveEmailVerificationToken(user, newToken);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);
            log.info("Verification email resent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend verification email: {}", e.getMessage());
            throw new RuntimeException("Failed to resend verification email", e);
        }
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Invalidate any existing tokens
        passwordResetTokenRepository.findByUser(user).ifPresent(token -> {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
        });

        // Generate and save password reset token
        String resetToken = UUID.randomUUID().toString();
        savePasswordResetToken(user, resetToken);

        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token already used");
        }

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
    
    private void saveEmailVerificationToken(User user, String token) {
        var verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(verificationTokenExpirationMinutes))
                .isUsed(false)
                .build();
        emailVerificationTokenRepository.save(verificationToken);
    }
    
    private void savePasswordResetToken(User user, String token) {
        var resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(passwordResetTokenExpirationMinutes))
                .isUsed(false)
                .build();
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return (User) authentication.getPrincipal();
    }
}
