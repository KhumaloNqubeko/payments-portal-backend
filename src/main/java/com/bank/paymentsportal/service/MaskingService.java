package com.bank.paymentsportal.service;

import org.springframework.stereotype.Service;

@Service
public class MaskingService {

    public String maskAccount(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        String lastFour = value.substring(value.length() - 4);
        return "*".repeat(Math.max(0, value.length() - 4)) + lastFour;
    }
}
