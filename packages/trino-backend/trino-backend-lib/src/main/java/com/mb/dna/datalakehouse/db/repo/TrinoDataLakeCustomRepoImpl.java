package com.mb.dna.datalakehouse.db.repo;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;

@Repository
public class TrinoDataLakeCustomRepoImpl extends CommonDataRepositoryImpl<TrinoDataLakeNsql, String>
		implements TrinoDataLakeCustomRepo {

}