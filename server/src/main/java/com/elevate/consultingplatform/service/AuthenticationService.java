package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.dto.auth.AuthenticationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationResponse;
import com.elevate.consultingplatform.dto.auth.RegisterRequest;
import com.elevate.consultingplatform.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    void verifyEmail(String verificationToken);
    void resendVerificationEmail(String email);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    User getCurrentUser();
}
