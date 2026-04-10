package com.bank.paymentsportal.dto;

import com.bank.paymentsportal.entity.UserRole;
import java.time.OffsetDateTime;

public record UserSummaryDto(
        Long id,
        String fullName,
        String username,
        String maskedAccountNumber,
        UserRole role,
        OffsetDateTime createdAt
) {
}
