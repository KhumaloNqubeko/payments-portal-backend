package com.bank.paymentsportal.dto;

import com.bank.paymentsportal.entity.CurrencyCode;
import com.bank.paymentsportal.entity.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "Sender full name is required")
        @Pattern(regexp = "^[A-Za-z' -]{2,100}$", message = "Sender full name contains invalid characters")
        String senderFullName,
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
        @NotBlank(message = "Beneficiary bank name is required")
        @Pattern(regexp = "^[A-Za-z0-9'.,&()\\-\\/ ]{2,120}$", message = "Beneficiary bank name contains invalid characters")
        String beneficiaryBankName,
        @NotBlank(message = "Beneficiary account number is required")
        @Pattern(regexp = "^[A-Z0-9]{8,34}$", message = "Beneficiary account number is invalid")
        String beneficiaryAccountNumber,
        @NotBlank(message = "SWIFT code is required")
        @Pattern(regexp = "^[A-Z0-9]{8}([A-Z0-9]{3})?$", message = "SWIFT code must be 8 or 11 uppercase alphanumeric characters")
        String swiftCode,
        @NotBlank(message = "Country is required")
        @Pattern(regexp = "^[A-Za-z ]{2,60}$", message = "Country contains invalid characters")
        String country,
        @NotBlank(message = "Payment reference is required")
        @Pattern(regexp = "^[A-Za-z0-9 .,'()\\-\\/]{3,140}$", message = "Payment reference contains invalid characters")
        String paymentReference
) {
}
