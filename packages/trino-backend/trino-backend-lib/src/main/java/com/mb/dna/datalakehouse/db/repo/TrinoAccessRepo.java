package com.mb.dna.datalakehouse.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;

public interface TrinoAccessRepo extends JpaRepository<TrinoAccessNsql, String> {

}
