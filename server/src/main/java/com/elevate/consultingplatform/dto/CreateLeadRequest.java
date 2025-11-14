package com.elevate.consultingplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLeadRequest {
    @NotBlank
    private String firstName;
    private String lastName;
    @NotBlank @Email
    private String email;
    private String phone;
    private String company;
    private Integer ainotseScore;
    private String status; // optional, defaults to Pending
    private String source;
    private String ainotseSummary;
}
