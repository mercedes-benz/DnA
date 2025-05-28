package com.daimler.data.db.repo.DbService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.DbServiceNsql;

@Repository
public interface DbServiceRepository extends JpaRepository<DbServiceNsql,String> {

}
