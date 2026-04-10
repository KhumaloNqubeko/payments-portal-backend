package com.bank.paymentsportal.entity;

public enum TransactionStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    SUBMITTED_TO_SWIFT,
    REJECTED
}
