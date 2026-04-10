package com.bank.paymentsportal.service;

import com.bank.paymentsportal.entity.AuditLog;
import com.bank.paymentsportal.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long actorId, String action, String entityType, Long entityId, String metadata) {
        auditLogRepository.save(AuditLog.builder()
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .build());
    }
}
