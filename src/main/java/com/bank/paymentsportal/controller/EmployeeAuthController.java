package com.bank.paymentsportal.controller;

import com.bank.paymentsportal.dto.AuthRequest;
import com.bank.paymentsportal.dto.AuthResponse;
import com.bank.paymentsportal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/auth")
public class EmployeeAuthController {

    private final AuthService authService;

    public EmployeeAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginEmployee(request));
    }
}
