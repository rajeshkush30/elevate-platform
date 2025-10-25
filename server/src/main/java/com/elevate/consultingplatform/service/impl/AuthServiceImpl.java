package com.elevate.consultingplatform.service.impl;

import com.elevate.consultingplatform.dto.auth.AuthenticationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationResponse;
import com.elevate.consultingplatform.dto.auth.RegisterRequest;
import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.AccountStatus;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Date;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import com.elevate.consultingplatform.mapper.UserMapper;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.security.JwtTokenUtil;
import com.elevate.consultingplatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already in use");
        }

        // Create new user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT) // Default role
                .isEmailVerified(false)
                .isActive(true)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate tokens
        return generateAuthResponse(savedUser);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        return generateAuthResponse(user);
    }

    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        // Extract username from refresh token
        String username = jwtTokenUtil.extractUsername(refreshToken);
        
        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // Validate refresh token
        if (!jwtTokenUtil.isTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // Get user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Generate new tokens
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        // In a real app, you would verify the token and mark the email as verified
        String email = jwtTokenUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        
        return true;
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // In a real app, you would generate a password reset token and send an email
        // For now, we'll just log it
        log.info("Password reset requested for user: {}", email);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        String email = jwtTokenUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // In a real app, you would validate the reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }

    @Override
    public AuthenticationResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return generateAuthResponse(user);
        }
        throw new BadCredentialsException("User not authenticated");
    }

    private AuthenticationResponse generateAuthResponse(User user) {
        // Generate access token
        Map<String, Object> extraClaims = new HashMap<>();
    // Guard against null role or account status
    var role = user.getRole() != null ? user.getRole() : Role.CLIENT;
    var accountStatus = user.getAccountStatus() != null ? user.getAccountStatus() : AccountStatus.PENDING_VERIFICATION;

    extraClaims.put("role", role.name());
        
        String accessToken = jwtTokenUtil.generateToken(extraClaims, user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);
        
        // Map user to response DTO
        UserResponse userResponse = userMapper.toUserResponse(user);
        
        // Build response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenUtil.getJwtExpirationMs() / 1000) // Convert to seconds
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(role)
                .isEmailVerified(user.isEmailVerified())
                .lastLogin(user.getLastLogin())
                .accountStatus(accountStatus.name())
                .issuedAt(new Date().toInstant())
                .expiresAt(new Date(System.currentTimeMillis() + jwtTokenUtil.getJwtExpirationMs()).toInstant())
                .build();
    }
}
