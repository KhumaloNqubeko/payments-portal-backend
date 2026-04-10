package com.bank.paymentsportal.dto;

import com.bank.paymentsportal.entity.CurrencyCode;
import com.bank.paymentsportal.entity.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
        BigDecimal amount,
        @NotNull(message = "Currency is required")
        CurrencyCode currency,
        @NotNull(message = "Provider is required")
        PaymentProvider provider,
        @NotBlank(message = "Beneficiary name is required")
        @Pattern(regexp = "^[A-Za-z0-9'.,&()\\-\\/ ]{2,100}$", message = "Beneficiary name contains invalid characters")
        String beneficiaryName,
        @NotBlank(message = "Beneficiary account number is required")
        @Pattern(regexp = "^[A-Z0-9]{8,34}$", message = "Beneficiary account number is invalid")
        String beneficiaryAccountNumber,
        @NotBlank(message = "SWIFT code is required")
        @Pattern(regexp = "^[A-Z0-9]{8}([A-Z0-9]{3})?$", message = "SWIFT code must be 8 or 11 uppercase alphanumeric characters")
        String swiftCode
) {
}
