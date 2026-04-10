package com.bank.paymentsportal.dto;

import com.bank.paymentsportal.entity.CurrencyCode;
import com.bank.paymentsportal.entity.PaymentProvider;
import com.bank.paymentsportal.entity.TransactionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        CurrencyCode currency,
        PaymentProvider provider,
        String beneficiaryName,
        String maskedBeneficiaryAccountNumber,
        String swiftCode,
        TransactionStatus status,
        String customerName,
        String customerMaskedAccountNumber,
        String verifiedByName,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt
) {
}
