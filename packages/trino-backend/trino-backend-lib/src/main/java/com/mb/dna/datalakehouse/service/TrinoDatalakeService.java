package com.mb.dna.datalakehouse.service;

import com.daimler.data.service.common.CommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

public interface TrinoDatalakeService extends CommonService<TrinoDataLakeProjectVO, TrinoDataLakeNsql, String> 
{
	
	Boolean isBucketExists(String bucketName);
	
}
