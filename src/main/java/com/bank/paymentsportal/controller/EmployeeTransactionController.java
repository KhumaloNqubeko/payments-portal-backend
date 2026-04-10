package com.bank.paymentsportal.controller;

import com.bank.paymentsportal.dto.TransactionResponse;
import com.bank.paymentsportal.security.CustomUserPrincipal;
import com.bank.paymentsportal.service.PaymentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/transactions")
public class EmployeeTransactionController {

    private final PaymentService paymentService;

    public EmployeeTransactionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactionsForEmployees());
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<TransactionResponse> verify(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(paymentService.verify(id, principal));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<TransactionResponse> reject(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(paymentService.reject(id, principal));
    }

    @PatchMapping("/{id}/submit-swift")
    public ResponseEntity<TransactionResponse> submit(@PathVariable Long id,
                                                      @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(paymentService.submitToSwift(id, principal));
    }
}
