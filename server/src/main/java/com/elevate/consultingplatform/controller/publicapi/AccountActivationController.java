package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.service.activation.AccountActivationService;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/account")
@RequiredArgsConstructor
@Tag(name = "Account Activation", description = "Public endpoints for activating user accounts")
public class AccountActivationController {

    private final AccountActivationService accountActivationService;

    @PostMapping("/activate")
    @PermitAll
    @Operation(summary = "Activate account with token", description = "Activates the account and sets a new password using the provided token")
    public ResponseEntity<ActivationResponse> activate(@RequestBody ActivationRequest req) {
        String email = accountActivationService.activate(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(new ActivationResponse(email));
    }

    @Data
    public static class ActivationRequest {
        private String token;
        private String newPassword;
    }

    @Data
    public static class ActivationResponse {
        private final String email;
    }
}
