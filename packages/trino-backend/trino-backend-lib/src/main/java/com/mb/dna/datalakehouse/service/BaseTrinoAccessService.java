package com.mb.dna.datalakehouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.assembler.TrinoAccessAssembler;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessRepo;
import com.mb.dna.datalakehouse.dto.TrinoAccessVO;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseTrinoAccessService extends BaseCommonService<TrinoAccessVO, TrinoAccessNsql, String>
		implements TrinoAccessService {
	
	@Autowired
	private TrinoAccessCustomRepo customRepo;
	
	@Autowired
	private TrinoAccessRepo jpaRepo;
	
	@Autowired
	private TrinoAccessAssembler assembler;

	public BaseTrinoAccessService() {
		super();
	}
	
}
