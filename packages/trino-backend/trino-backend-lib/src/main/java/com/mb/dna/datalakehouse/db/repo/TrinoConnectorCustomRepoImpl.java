package com.mb.dna.datalakehouse.db.repo;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;

@Repository
public class TrinoConnectorCustomRepoImpl extends CommonDataRepositoryImpl<TrinoConnectorNsql, String>
		implements TrinoConnectorCustomRepo {

}