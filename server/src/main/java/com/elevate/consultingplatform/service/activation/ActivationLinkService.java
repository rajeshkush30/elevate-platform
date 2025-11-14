package com.elevate.consultingplatform.service.activation;

import com.elevate.consultingplatform.entity.AccountStatus;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.zoholeads.LeadRepository;
import com.elevate.consultingplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Builds activation links for new users approved in Zoho.
 * Performs an upsert for a pending CLIENT user.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationLinkService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LeadRepository leadRepository;
    @Value("${app.client.base-url:http://localhost:3000}")
    private String clientBaseUrl;

    /**
     * Creates or updates a pending user and issues a token for setting password.
     * Returns an activation URL suitable for embedding in Zoho email templates.
     */
    public String buildActivationLink(String email, String contactId) {
        // Upsert user with PENDING status and CLIENT role
        var existing = leadRepository.findByZohoLeadId(contactId)
                .orElseThrow(() -> new RuntimeException("Lead not approved or not found for contactId: " + contactId));



        User user = User.builder()
                    .firstName(existing.getFirstName())
                    .lastName(existing.getLastName())
                    .email(email)
                    .phoneNumber(existing.getPhone())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // temp password
                    .role(Role.CLIENT)
                    .isEmailVerified(false)
                    .isActive(true)
                    .accountStatus(AccountStatus.PENDING_VERIFICATION)
                    .build();
            userRepository.save(user);
            log.info("[ActivationLinkService] Created pending CLIENT user for {} (contactId={})", email, contactId);


        // Build a short-lived token using JwtService (subject = email)
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
        UserDetails principal = new org.springframework.security.core.userdetails.User(email, "", authorities);
        String token = jwtService.generateToken(principal);

        String url = clientBaseUrl + "/activate-account?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        log.info("[ActivationLinkService] Activation URL generated for {} -> {}", email, url);
        return url;
    }
}
