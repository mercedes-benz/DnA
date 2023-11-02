package com.mb.dna.datalakehouse.service;

import com.daimler.data.service.common.CommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.dto.TrinoAccessVO;

public interface TrinoAccessService extends CommonService<TrinoAccessVO, TrinoAccessNsql, String> 
{
	
}
