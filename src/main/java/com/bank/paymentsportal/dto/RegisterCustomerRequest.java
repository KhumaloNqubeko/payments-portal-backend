package com.bank.paymentsportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank(message = "Full name is required")
        @Pattern(regexp = "^[A-Za-z' -]{2,100}$", message = "Full name contains invalid characters")
        String fullName,
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[A-Za-z0-9._]{3,30}$", message = "Username must be 3-30 characters and use letters, digits, underscore or dot")
        String username,
        @NotBlank(message = "Email is required")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]{3,64}@[A-Za-z0-9.-]{2,253}\\.[A-Za-z]{2,20}$",
                message = "Email address is invalid"
        )
        String email,
        @NotBlank(message = "South African ID number is required")
        @Pattern(regexp = "^\\d{13}$", message = "South African ID number must be exactly 13 digits")
        String southAfricanIdNumber,
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "^\\d{8,12}$", message = "Account number must be 8-12 digits")
        String accountNumber,
        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 128, message = "Password must be at least 12 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,128}$",
                message = "Password must contain uppercase, lowercase, digit and special character"
        )
        String password
) {
}
