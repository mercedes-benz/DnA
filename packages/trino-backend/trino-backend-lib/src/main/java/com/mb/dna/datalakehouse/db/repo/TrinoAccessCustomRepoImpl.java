package com.mb.dna.datalakehouse.db.repo;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;

@Repository
public class TrinoAccessCustomRepoImpl extends CommonDataRepositoryImpl<TrinoAccessNsql, String>
		implements TrinoAccessCustomRepo {

}