package com.daimler.data.db.repo.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.StorageNsql;

@Repository
public interface IStorageRepository extends JpaRepository<StorageNsql, String>{

}
