package com.mb.dna.datalakehouse.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;

public interface TrinoDataLakeRepo extends JpaRepository<TrinoDataLakeNsql, String> {

}
