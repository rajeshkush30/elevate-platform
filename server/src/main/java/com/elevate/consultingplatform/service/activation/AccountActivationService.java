package com.elevate.consultingplatform.service.activation;

import com.elevate.consultingplatform.entity.AccountStatus;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountActivationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates the token and sets the password for the user identified by the token subject (email).
     * Returns the activated email for confirmation.
     */
    public String activate(String token, String newPassword) {
        // Extract email (subject) from JWT
        String email = jwtService.extractUsername(token);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Invalid activation token: missing subject");
        }

        // Load or create user (defensive). In normal flow user exists from ActivationLinkService upsert.
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String first = email.split("@", 2)[0];
            return User.builder()
                    .firstName(first)
                    .lastName("")
                    .email(email)
                    .password("")
                    .role(Role.CLIENT)
                    .isEmailVerified(false)
                    .isActive(true)
                    .accountStatus(AccountStatus.PENDING_VERIFICATION)
                    .build();
        });

        // Set password and mark active/verified
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);

        log.info("[AccountActivation] Activated user email={} (status ACTIVE, verified)", email);
        return email;
    }
}
