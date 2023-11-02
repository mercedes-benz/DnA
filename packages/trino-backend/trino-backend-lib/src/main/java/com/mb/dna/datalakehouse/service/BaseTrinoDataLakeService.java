package com.mb.dna.datalakehouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.TrinoDataLakeAssembler;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeRepo;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class BaseTrinoDataLakeService extends BaseCommonService<TrinoDataLakeProjectVO, TrinoDataLakeNsql, String>
		implements TrinoDatalakeService {
	
	@Autowired
	private TrinoDataLakeCustomRepo customRepo;
	
	@Autowired
	private TrinoDataLakeRepo jpaRepo;
	
	@Autowired
	private TrinoDataLakeAssembler assembler;
	
	@Autowired
	private StorageServicesClient storageClient;

	public BaseTrinoDataLakeService() {
		super();
	}
	
	@Override
	@Transactional
	public Boolean isBucketExists(String bucketName) {
		return storageClient.isBucketExists(bucketName);
	}	
	
}
