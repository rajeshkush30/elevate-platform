package com.elevate.consultingplatform.service.impl;

import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.mapper.UserMapper;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.service.AdminClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.elevate.consultingplatform.entity.PasswordResetToken;
import com.elevate.consultingplatform.repository.PasswordResetTokenRepository;
import com.elevate.consultingplatform.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminClientServiceImpl implements AdminClientService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final com.elevate.consultingplatform.repository.EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(AdminClientServiceImpl.class);

    @Value("${app.security.password-reset-token.expiration-minutes:30}")
    private int passwordResetTokenExpirationMinutes;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllClients() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getClientById(Long id) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public com.elevate.consultingplatform.dto.user.CreateClientResponse createClient(UserResponse request) {
    log.debug("createClient request={}", request);
    // Prevent duplicate email
    if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.CONFLICT,
            "Client with email already exists"
        );
    }
    // Ensure required fields are set to avoid constraint violations
    String firstName = request.getFirstName() != null ? request.getFirstName() : "";
    String lastName = request.getLastName() != null ? request.getLastName() : "";
    String email = request.getEmail();

    // Generate a temporary password for the created client
    String tempPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    String encoded = null;
    try {
        encoded = passwordEncoder.encode(tempPassword);
    } catch (Exception ex) {
        log.warn("PasswordEncoder failed to encode generated password, falling back to default", ex);
    }
    if (encoded == null) {
        encoded = passwordEncoder.encode("ChangeMe123!");
        // if encoding default succeeded, override tempPassword to the known value so admin can share it
        tempPassword = "ChangeMe123!";
    }
    log.debug("Generated tempPassword present, encoded length={}", encoded != null ? encoded.length() : 0);

    User user = User.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .password(encoded)
        .role(com.elevate.consultingplatform.entity.Role.CLIENT)
        .isActive(true)
        .isEmailVerified(true)
        .build();
    try {
        var saved = userRepository.save(user);

        // Create password reset token for the new user so they can set their own password.
        String resetTokenStr = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(resetTokenStr)
                .user(saved)
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(passwordResetTokenExpirationMinutes))
                .isUsed(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send password reset email asynchronously
        try {
            emailService.sendPasswordResetEmail(saved.getEmail(), saved.getFullName(), resetTokenStr);
        } catch (Exception ex) {
            log.warn("Failed to send password reset email for user {}", saved.getEmail(), ex);
        }

        var userResp = userMapper.toUserResponse(saved);
        return com.elevate.consultingplatform.dto.user.CreateClientResponse.builder()
                .user(userResp)
                .temporaryPassword(tempPassword)
                .build();
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        // Log full exception for debugging in server logs
        log.error("Failed to save user due to DataIntegrityViolation", ex);
        throw ex;
    }
    }

    @Override
    @Transactional
    public UserResponse updateClient(Long id, UserResponse request) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
        // Basic fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(request.getRole());

        // Booleans from request (Jackson maps JSON {"active":true} to isActive field)
        user.setActive(request.isActive());
        user.setEmailVerified(request.isEmailVerified());

        // Touch updatedAt if the entity supports it
        try {
            java.lang.reflect.Method setter = user.getClass().getMethod("setUpdatedAt", java.time.LocalDateTime.class);
            setter.invoke(user, java.time.LocalDateTime.now());
        } catch (Exception ignore) { /* updatedAt may be handled by JPA auditing */ }

        var saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        // Remove any tokens that reference this user to avoid foreign key constraint failures
        var userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            // Delete password reset token if present
            passwordResetTokenRepository.findByUser(user).ifPresent(token -> passwordResetTokenRepository.delete(token));
            // Delete email verification token if present
            emailVerificationTokenRepository.findByUser(user).ifPresent(token -> emailVerificationTokenRepository.delete(token));
            // Now delete the user
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Client not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<UserResponse> searchClients(String query, org.springframework.data.domain.Pageable pageable) {
        String q = query == null ? "" : query.trim();
        var page = userRepository.searchClients(com.elevate.consultingplatform.entity.Role.CLIENT, q, pageable);
        return page.map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public void resendInvite(Long id) {
        var user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
        // Create new password reset token
        String resetTokenStr = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(resetTokenStr)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(passwordResetTokenExpirationMinutes))
                .isUsed(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetTokenStr);
        } catch (Exception ex) {
            log.warn("Failed to send password reset email for user {}", user.getEmail(), ex);
        }
    }
}
