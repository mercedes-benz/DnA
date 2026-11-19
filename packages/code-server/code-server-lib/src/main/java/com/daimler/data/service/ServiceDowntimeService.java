package com.daimler.data.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.db.entities.ServiceDowntimeEntity;
import com.daimler.data.db.repo.workspace.ServiceDowntimeRepository;
import com.daimler.data.dto.ServiceDowntimeVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceDowntimeService {

    private static final long ACTIVE_CACHE_TTL_MS = 30_000L;

    @Autowired
    private ServiceDowntimeRepository repository;

    private volatile ActiveSnapshot activeSnapshot;

    public List<ServiceDowntimeVO> getActiveDowntimes() {
        ActiveSnapshot snapshot = activeSnapshot;
        long now = System.currentTimeMillis();
        if (snapshot != null && now - snapshot.loadedAt < ACTIVE_CACHE_TTL_MS) {
            return snapshot.downtimes;
        }
        List<ServiceDowntimeVO> downtimes = Collections.unmodifiableList(repository.findByIsActiveTrue().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));
        activeSnapshot = new ActiveSnapshot(downtimes, now);
        return downtimes;
    }

    public void markDown(String serviceName, String message) {
        LocalDateTime now = LocalDateTime.now();
        ServiceDowntimeEntity entity = repository.findByServiceName(serviceName).orElseGet(() -> {
            ServiceDowntimeEntity newEntity = new ServiceDowntimeEntity();
            newEntity.setId(UUID.randomUUID().toString());
            newEntity.setServiceName(serviceName);
            newEntity.setDetectedAt(now);
            return newEntity;
        });
        entity.setMessage(message);
        entity.setActive(true);
        entity.setLastChecked(now);
        entity.setResolvedAt(null);
        repository.save(entity);
        activeSnapshot = null;
        log.warn("Service downtime detected: {} - {}", serviceName, message);
    }

    public void markUp(String serviceName) {
        repository.findByServiceName(serviceName).ifPresent(entity -> {
            if (entity.isActive()) {
                LocalDateTime now = LocalDateTime.now();
                entity.setActive(false);
                entity.setResolvedAt(now);
                entity.setLastChecked(now);
                repository.save(entity);
                activeSnapshot = null;
                log.info("Service recovered: {}", serviceName);
            }
        });
    }

    private ServiceDowntimeVO toVO(ServiceDowntimeEntity entity) {
        ServiceDowntimeVO vo = new ServiceDowntimeVO();
        vo.setId(entity.getId());
        vo.setServiceName(entity.getServiceName());
        vo.setMessage(entity.getMessage());
        vo.setDetectedAt(entity.getDetectedAt() == null ? null : entity.getDetectedAt().toString());
        return vo;
    }

    private static final class ActiveSnapshot {
        private final List<ServiceDowntimeVO> downtimes;
        private final long loadedAt;

        private ActiveSnapshot(List<ServiceDowntimeVO> downtimes, long loadedAt) {
            this.downtimes = downtimes;
            this.loadedAt = loadedAt;
        }
    }
}