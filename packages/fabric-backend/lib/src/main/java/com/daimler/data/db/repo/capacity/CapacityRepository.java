package com.daimler.data.db.repo.capacity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.data.db.entities.CapacityNsql;

public interface CapacityRepository extends JpaRepository<CapacityNsql, String> {
    
}
