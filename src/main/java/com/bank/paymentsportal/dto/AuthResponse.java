package com.bank.paymentsportal.dto;

import com.bank.paymentsportal.entity.UserRole;

public record AuthResponse(
        String token,
        UserRole role,
        UserSummaryDto user
) {
}
