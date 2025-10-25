package com.elevate.consultingplatform.dto.auth;

import com.elevate.consultingplatform.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegistrationRequest {
    @Valid
    @NotNull(message = "User details are required")
    private RegisterRequest userDetails;

    @NotNull(message = "Role is required")
    private Role role;
}
