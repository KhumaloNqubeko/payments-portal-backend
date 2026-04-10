package com.bank.paymentsportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(nullable = false, length = 500)
    private String metadata;

    @PrePersist
    void onCreate() {
        timestamp = OffsetDateTime.now();
    }
}
