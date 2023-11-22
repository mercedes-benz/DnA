package com.mb.dna.datalakehouse.db.repo;

import java.util.List;

import com.daimler.data.db.repo.common.CommonDataRepository;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;

public interface TrinoDataLakeCustomRepo extends CommonDataRepository<TrinoDataLakeNsql, String> {

	List<TrinoDataLakeNsql> getAll(String userId, int offset, int limit);

	Long getCount(String user);

	Long getCountByUserAndProject(String userId, String id);

}
