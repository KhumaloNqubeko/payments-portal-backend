package com.bank.paymentsportal.repository;

import com.bank.paymentsportal.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
