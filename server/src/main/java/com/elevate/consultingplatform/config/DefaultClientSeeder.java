package com.elevate.consultingplatform.config;

import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DefaultClientSeeder {

    private static final Logger log = LoggerFactory.getLogger(DefaultClientSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.client.email:}")
    private String clientEmail;

    @Value("${app.client.password:}")
    private String clientPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createDefaultClientIfConfigured() {
        if (clientEmail == null || clientEmail.isBlank() || clientPassword == null || clientPassword.isBlank()) {
            return; // not configured
        }
        userRepository.findByEmail(clientEmail).ifPresentOrElse(u -> {
            // Ensure still active and email verified to allow login
            if (!u.isActive()) { u.setActive(true); }
            if (!u.isEmailVerified()) { u.setEmailVerified(true); }
            if (u.getRole() != Role.CLIENT) { u.setRole(Role.CLIENT); }
            log.info("Default client '{}' already exists", clientEmail);
        }, () -> {
            User user = User.builder()
                    .firstName("Client")
                    .lastName("User")
                    .email(clientEmail)
                    .password(passwordEncoder.encode(clientPassword))
                    .role(Role.CLIENT)
                    .isEmailVerified(true)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Created default client '{}' via app.client.* configuration", clientEmail);
        });
    }
}
