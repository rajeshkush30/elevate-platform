package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.dto.auth.AuthenticationRequest;
import com.elevate.consultingplatform.dto.auth.AuthenticationResponse;
import com.elevate.consultingplatform.dto.auth.RegisterRequest;
import com.elevate.consultingplatform.entity.User;

public interface AuthService {
    /**
     * Register a new user
     * @param request The registration request containing user details
     * @return Authentication response with JWT tokens
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticate a user
     * @param request The authentication request containing credentials
     * @return Authentication response with JWT tokens
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Refresh an access token using a refresh token
     * @param refreshToken The refresh token
     * @return New authentication response with fresh tokens
     */
    AuthenticationResponse refreshToken(String refreshToken);

    /**
     * Verify a user's email using a verification token
     * @param token The verification token
     * @return True if verification was successful
     */
    boolean verifyEmail(String token);

    /**
     * Initiate password reset for a user
     * @param email The user's email
     */
    void forgotPassword(String email);

    /**
     * Reset a user's password using a reset token
     * @param token The password reset token
     * @param newPassword The new password
     * @return True if password was reset successfully
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * Get the currently authenticated user with authentication details
     * @return Authentication response with user details and tokens
     */
    AuthenticationResponse getCurrentUser();
}
