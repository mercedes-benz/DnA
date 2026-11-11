package com.daimler.data.db.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "service_downtime")
public class ServiceDowntimeEntity {

    @Id
    private String id;

    @Column(name = "service_name", nullable = false, unique = true)
    private String serviceName;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;
}
