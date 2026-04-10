package com.bank.paymentsportal.controller;

import com.bank.paymentsportal.dto.PaymentRequest;
import com.bank.paymentsportal.dto.TransactionResponse;
import com.bank.paymentsportal.security.CustomUserPrincipal;
import com.bank.paymentsportal.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createPayment(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                             @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(principal, request));
    }

    @GetMapping("/my-transactions")
    public ResponseEntity<List<TransactionResponse>> myTransactions(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(paymentService.getMyTransactions(principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                              @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getMyTransaction(principal, id));
    }
}
