package com.elevate.consultingplatform.controller.integration;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.entity.training.ProgressStatus;
import com.elevate.consultingplatform.entity.training.UserStageProgress;
import com.elevate.consultingplatform.repository.UserRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.repository.training.UserStageProgressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations/lms")
@RequiredArgsConstructor
public class LMSWebhookController {

    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final UserStageProgressRepository progressRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${lms.webhook.secret:changeme}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestHeader(value = "X-Signature", required = false) String signature,
                                        @RequestBody Map<String, Object> body) {
        if (!verify(signature, body)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String event = str(body.get("event"));
        if ("MODULE_COMPLETED".equalsIgnoreCase(event)) {
            String email = str(body.get("clientEmail"));
            Long stageId = asLong(body.get("stageId"));
            Double score = asDouble(body.get("score"));
            String evidenceUrl = str(body.get("evidenceUrl"));
            if (email != null && stageId != null) {
                userRepository.findByEmail(email).ifPresent(user -> completeStage(user, stageId, score, evidenceUrl));
            }
        }

        return ResponseEntity.ok().build();
    }

    private boolean verify(String signature, Map<String, Object> body) {
        if (signature == null || signature.isBlank()) return false;
        try {
            String payload = mapper.writeValueAsString(body);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(digest);
            return expected.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private void completeStage(User user, Long stageId, Double score, String evidenceUrl) {
        Stage stage = stageRepository.findById(stageId).orElse(null);
        if (stage == null) return;
        UserStageProgress p = progressRepository.findByUserAndStage(user, stage)
                .orElse(UserStageProgress.builder().user(user).stage(stage).build());
        if (p.getStartedAt() == null) p.setStartedAt(LocalDateTime.now());
        p.setStatus(ProgressStatus.COMPLETED);
        p.setCompletedAt(LocalDateTime.now());
        p.setScore(score);
        p.setEvidenceUrl(evidenceUrl);
        progressRepository.save(p);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static Long asLong(Object o) { try { return o == null ? null : Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
    private static Double asDouble(Object o) { try { return o == null ? null : Double.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
}
