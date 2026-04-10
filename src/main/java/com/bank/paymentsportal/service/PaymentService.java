package com.bank.paymentsportal.service;

import com.bank.paymentsportal.dto.PaymentRequest;
import com.bank.paymentsportal.dto.TransactionResponse;
import com.bank.paymentsportal.entity.PaymentTransaction;
import com.bank.paymentsportal.entity.TransactionStatus;
import com.bank.paymentsportal.entity.User;
import com.bank.paymentsportal.exception.ApiException;
import com.bank.paymentsportal.repository.PaymentTransactionRepository;
import com.bank.paymentsportal.repository.UserRepository;
import com.bank.paymentsportal.security.CustomUserPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final MaskingService maskingService;

    public PaymentService(PaymentTransactionRepository transactionRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService,
                          MaskingService maskingService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.maskingService = maskingService;
    }

    @Transactional
    public TransactionResponse createPayment(CustomUserPrincipal principal, PaymentRequest request) {
        User customer = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));

        PaymentTransaction transaction = transactionRepository.save(PaymentTransaction.builder()
                .customer(customer)
                .amount(request.amount())
                .currency(request.currency())
                .provider(request.provider())
                .beneficiaryName(request.beneficiaryName().trim())
                .beneficiaryAccountNumber(request.beneficiaryAccountNumber())
                .swiftCode(request.swiftCode())
                .status(TransactionStatus.PENDING_VERIFICATION)
                .build());
        auditLogService.log(principal.getId(), "CREATE_PAYMENT", "TRANSACTION", transaction.getId(), "status=PENDING_VERIFICATION");
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(CustomUserPrincipal principal) {
        return transactionRepository.findAllByCustomerIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getMyTransaction(CustomUserPrincipal principal, Long id) {
        PaymentTransaction transaction = transactionRepository.findByIdAndCustomerId(id, principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsForEmployees() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse verify(Long transactionId, CustomUserPrincipal principal) {
        PaymentTransaction transaction = getEmployeeTransaction(transactionId);
        if (transaction.getStatus() != TransactionStatus.PENDING_VERIFICATION) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending transactions can be verified");
        }
        transaction.setStatus(TransactionStatus.VERIFIED);
        transaction.setVerifiedBy(principal.getUser());
        auditLogService.log(principal.getId(), "VERIFY_PAYMENT", "TRANSACTION", transaction.getId(), "status=VERIFIED");
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse reject(Long transactionId, CustomUserPrincipal principal) {
        PaymentTransaction transaction = getEmployeeTransaction(transactionId);
        if (transaction.getStatus() == TransactionStatus.SUBMITTED_TO_SWIFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Submitted transactions cannot be rejected");
        }
        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setVerifiedBy(principal.getUser());
        auditLogService.log(principal.getId(), "REJECT_PAYMENT", "TRANSACTION", transaction.getId(), "status=REJECTED");
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse submitToSwift(Long transactionId, CustomUserPrincipal principal) {
        PaymentTransaction transaction = getEmployeeTransaction(transactionId);
        if (transaction.getStatus() != TransactionStatus.VERIFIED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only verified transactions can be submitted to SWIFT");
        }
        transaction.setStatus(TransactionStatus.SUBMITTED_TO_SWIFT);
        transaction.setVerifiedBy(principal.getUser());
        transaction.setSubmittedAt(OffsetDateTime.now());
        auditLogService.log(principal.getId(), "SUBMIT_TO_SWIFT", "TRANSACTION", transaction.getId(), "provider=SWIFT");
        return toResponse(transaction);
    }

    private PaymentTransaction getEmployeeTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private TransactionResponse toResponse(PaymentTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getProvider(),
                transaction.getBeneficiaryName(),
                maskingService.maskAccount(transaction.getBeneficiaryAccountNumber()),
                transaction.getSwiftCode(),
                transaction.getStatus(),
                transaction.getCustomer().getFullName(),
                maskingService.maskAccount(transaction.getCustomer().getAccountNumber()),
                transaction.getVerifiedBy() != null ? transaction.getVerifiedBy().getFullName() : null,
                transaction.getSubmittedAt(),
                transaction.getCreatedAt()
        );
    }
}
