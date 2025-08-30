package com.ecclesiaflow.business.services;

import com.ecclesiaflow.web.exception.ConfirmationEmailException;
import com.ecclesiaflow.web.exception.PasswordResetEmailException;
import com.ecclesiaflow.web.exception.WelcomeEmailException;

public interface EmailService {
    void sendConfirmationCode(String email, String code, String firstName) throws ConfirmationEmailException;
    void sendWelcomeEmail(String email, String firstName) throws WelcomeEmailException;
    void sendPasswordResetEmail(String email, String resetLink, String firstName) throws PasswordResetEmailException;
}
