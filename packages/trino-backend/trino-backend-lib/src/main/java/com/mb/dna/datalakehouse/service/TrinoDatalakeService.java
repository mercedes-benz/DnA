package com.mb.dna.datalakehouse.service;

import java.util.List;

import com.daimler.data.service.common.CommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

public interface TrinoDatalakeService extends CommonService<TrinoDataLakeProjectVO, TrinoDataLakeNsql, String> 
{
	
	Boolean isBucketExists(String bucketName);

	TrinoDataLakeProjectResponseVO createDatalake(TrinoDataLakeProjectVO vo) throws Exception;

	List<String> showSchemas(String catalogName, String schemaName);

	List<TrinoDataLakeProjectVO> getAll(int limit, int offset, String user);

	Long getCount( String user);

	Long getCountForUserAndProject( String user, String projectId);
	
}
