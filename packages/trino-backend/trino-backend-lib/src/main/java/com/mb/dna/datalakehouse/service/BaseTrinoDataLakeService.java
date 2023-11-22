package com.mb.dna.datalakehouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.TrinoDataLakeAssembler;
import com.daimler.data.dna.trino.config.TrinoClient;
import com.daimler.data.dto.UserInfoVO;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeRepo;
import com.mb.dna.datalakehouse.dto.DataLakeTableCollabDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
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
	
	@Autowired
	private TrinoClient trinoClient;

	private static String createSchema = "CREATE SCHEMA IF NOT EXISTS ";
	
	public BaseTrinoDataLakeService() {
		super();
	}
	
	@Override
	@Transactional
	public Boolean isBucketExists(String bucketName) {
		return storageClient.isBucketExists(bucketName);
	}

	@Override
	@Transactional
	public TrinoDataLakeProjectVO createDatalake(TrinoDataLakeProjectVO vo) throws Exception{
		
		List<UserInfoVO> collaborators = new ArrayList<>();
		collaborators.add(vo.getCreatedBy());
		if(vo.getTables()!=null && !vo.getTables().isEmpty()) {
			for(DatalakeTableVO tableVO : vo.getTables()) {
				if(tableVO.getCollabs()!=null && !tableVO.getCollabs().isEmpty()) {
					for(DataLakeTableCollabDetailsVO collab: tableVO.getCollabs()) {
						if(collab!=null && collab.getCollaborator()!=null && collab.getCollaborator().getId()!=null)
							collaborators.add(collab.getCollaborator());
					}
				}
			}
		}
		CreateBucketResponseWrapperDto bucketCreationResponse = storageClient.createBucket(vo.getBucketName(),collaborators);
		if(bucketCreationResponse!= null && "SUCCESS".equalsIgnoreCase(bucketCreationResponse.getStatus())) {
			vo.setBucketId(bucketCreationResponse.getData().getId());
			log.info("Bucket {} has been successfully created for project {}");
			
			String externalLocation = "s3a://"+vo.getBucketName();
			String createSchemaStatement = createSchema + vo.getCatalogName() + "." + vo.getSchemaName() + " WITH (location = '" + externalLocation + "')";
			try {
				trinoClient.executeStatments(createSchemaStatement);
				log.info("Successfully created Schema named {} at catalog {} at datalake project creation", vo.getSchemaName(), vo.getCatalogName());
			}catch(Exception e) {
				log.error("Failed while executing create schema statement {} at datalake project creation, with exception {}", createSchemaStatement, e.getMessage());
				throw new Exception("Error while executing create schema statement at Datalake project creation.");
			}
			TrinoDataLakeProjectVO datalakeProjectVO = super.create(vo);
			return datalakeProjectVO;
		}else {
			log.error("Failed while creating bucket {}  for Datalake project, to store metadata. Create Datalake Project Failed.", vo.getBucketName());
			throw new Exception("Failed while creating bucket" + vo.getBucketName() + " for Datalake project, to store metadata. Create Datalake Project Failed.");
		}
	}

	@Override
	public List<String> showSchemas(String catalogName, String schemaName) {
		return trinoClient.showSchemas(catalogName, schemaName);
	}	
	
	
	@Override
	public List<TrinoDataLakeProjectVO> getAll( int limit,  int offset, String user) {
		List<TrinoDataLakeNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}
	
	@Override
	public Long getCountForUserAndProject(String user, String projectId) {
		return customRepo.getCountByUserAndProject(user, projectId);
	}
	
	@Override
	public Long getCount( String user) {
		return customRepo.getCount(user);
	}
	
	
}
