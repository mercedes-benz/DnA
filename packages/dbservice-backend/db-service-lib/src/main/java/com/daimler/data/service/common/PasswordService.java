package com.daimler.data.service.common;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PASSWORD_LENGTH = 20;

    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            password.append(ALPHANUMERIC.charAt(index));
        }
        return password.toString();
    }

}
