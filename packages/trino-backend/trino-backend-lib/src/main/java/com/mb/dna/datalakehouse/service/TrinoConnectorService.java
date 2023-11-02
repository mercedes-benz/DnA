package com.mb.dna.datalakehouse.service;

import com.daimler.data.service.common.CommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.dto.TrinoConnectorVO;

public interface TrinoConnectorService extends CommonService<TrinoConnectorVO, TrinoConnectorNsql, String> 
{
	
}
