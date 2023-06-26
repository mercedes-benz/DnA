package com.mb.dna.data.dataiku.db.repo;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.mb.dna.data.dataiku.db.entities.DataikuSql;

public interface DataikuRepository {

	List<DataikuSql> getAll(String userId, int offset, int limit, String sortBy, String sortOrder, String projectName);

	BigInteger getTotalCount(String userId, String projectName);

	Optional<DataikuSql> findById(String id);

	void deleteById(String id);

	void save(DataikuSql dataikuProject);

	void update(DataikuSql dataikuProject);

	DataikuSql findByProjectName(String projectName, String cloudProfile);
	
	void updateSolutionForDataiku(String projectName, String cloudProfile, String solutionId);

}
