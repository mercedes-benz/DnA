package com.mb.dna.datalakehouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.assembler.TrinoConnectorAssembler;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoConnectorCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoConnectorRepo;
import com.mb.dna.datalakehouse.dto.TrinoConnectorVO;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseTrinoConnectorService extends BaseCommonService<TrinoConnectorVO, TrinoConnectorNsql, String>
		implements TrinoConnectorService {
	
	@Autowired
	private TrinoConnectorCustomRepo customRepo;
	
	@Autowired
	private TrinoConnectorRepo jpaRepo;
	
	@Autowired
	private TrinoConnectorAssembler assembler;

	public BaseTrinoConnectorService() {
		super();
	}
	
}
