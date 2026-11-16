package com.daimler.data.db.repo.workspace;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.data.db.entities.ServiceDowntimeEntity;

public interface ServiceDowntimeRepository extends JpaRepository<ServiceDowntimeEntity, String> {

    List<ServiceDowntimeEntity> findByIsActiveTrue();

    Optional<ServiceDowntimeEntity> findByServiceName(String serviceName);
}
