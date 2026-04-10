package com.bank.paymentsportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "Username or account number is required")
        @Pattern(regexp = "^[A-Za-z0-9._]{3,50}$|^[0-9]{8,12}$",
                message = "Use a valid username or account number")
        String usernameOrAccountNumber,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password is required")
        String password
) {
}
