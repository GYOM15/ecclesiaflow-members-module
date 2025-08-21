package com.ecclesiaflow.business.services;

public interface EmailService {
    void sendConfirmationCode(String email, String code, String firstName);
    void sendWelcomeEmail(String email, String firstName);
}
