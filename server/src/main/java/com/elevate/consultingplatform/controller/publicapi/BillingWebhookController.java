package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.entity.AccountStatus;
import com.elevate.consultingplatform.entity.Role;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.service.billing.TrainingEntitlementService;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/zoho")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Webhook", description = "Zoho billing payment webhook for entitlements")
public class BillingWebhookController {

    private final UserRepository userRepository;
    private final TrainingEntitlementService entitlementService;

    @PostMapping("/payment-webhook")
    @PermitAll
    @Operation(summary = "Handle payment webhook", description = "Processes Zoho payment_success events and grants training entitlements")
    public ResponseEntity<Void> paymentWebhook(@RequestBody PaymentEvent event) {
        log.info("[ZohoPaymentWebhook] Received event: {} {} {} {}", event.getEvent(), event.getEmail(), event.getStageId(), event.getOrderRef());

        if (!"payment_success".equalsIgnoreCase(event.getEvent())) {
            return ResponseEntity.ok().build(); // ignore non-success events for now
        }

        // Upsert user by email (defensive)
        User user = userRepository.findByEmail(event.getEmail()).orElseGet(() -> {
            String first = event.getEmail().split("@", 2)[0];
            return User.builder()
                    .firstName(first)
                    .lastName("")
                    .email(event.getEmail())
                    .password("")
                    .role(Role.CLIENT)
                    .isEmailVerified(true) // already email-verified due to activation flow
                    .isActive(true)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
        });
        if (user.getId() == null) user = userRepository.save(user);

        // Use stageId as entitlement scope key for MVP
        String scopeKey = String.valueOf(event.getStageId());
        entitlementService.grant(user, scopeKey, "ZOHO_PAYMENT", event.getOrderRef());

        // TODO: Optionally trigger LMS assignment here using stageId

        return ResponseEntity.ok().build();
    }

    @Data
    public static class PaymentEvent {
        private String event;            // expected: payment_success
        private String email;            // user identifier
        private String contactId;        // optional Zoho Contact id
        private Long stageId;            // the stage to unlock (MVP uses stageId)
        private String orderRef;         // platform or Zoho order reference
        private Long amount;             // minor units (optional)
        private String currency;         // e.g., INR
        private OffsetDateTime paidAt;   // optional
        private String signature;        // optional for future HMAC verification
    }
}
