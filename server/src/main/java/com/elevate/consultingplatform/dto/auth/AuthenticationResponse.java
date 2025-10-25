package com.elevate.consultingplatform.dto.auth;

import com.elevate.consultingplatform.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    // User information
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePictureUrl;
    private Role role;
    private boolean isEmailVerified;
    private LocalDateTime lastLogin;
    
    // Additional metadata
    @JsonProperty("account_status")
    private String accountStatus;
    
    // Token expiration timestamps
    @JsonProperty("issued_at")
    private java.time.Instant issuedAt;
    
    @JsonProperty("expires_at")
    private java.time.Instant expiresAt;
}
