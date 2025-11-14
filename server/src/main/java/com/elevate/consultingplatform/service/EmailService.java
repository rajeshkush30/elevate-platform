package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.entity.zoholeads.Lead;

public interface EmailService {
    void sendVerificationEmail(String to, String name, String token);
    void sendPasswordResetEmail(String to, String name, String token);
    void sendWelcomeEmail(String to, String name);

    void sendApprovedEmail(Lead lead);
    void sendRejectedEmail(Lead lead);
}
