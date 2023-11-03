package com.mb.dna.datalakehouse.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;

public interface TrinoConnectorRepo extends JpaRepository<TrinoConnectorNsql, String> {

}
