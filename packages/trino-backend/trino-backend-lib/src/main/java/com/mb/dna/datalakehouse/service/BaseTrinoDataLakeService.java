package com.mb.dna.datalakehouse.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.TrinoDataLakeAssembler;
import com.daimler.data.assembler.TrinoTableUtility;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dna.trino.config.TrinoClient;
import com.daimler.data.dto.UserInfoVO;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.repo.TrinoConnectorRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeRepo;
import com.mb.dna.datalakehouse.dto.DataLakeTableCollabDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
import com.mb.dna.datalakehouse.dto.GenerateTableStmtResponseVO;
import com.mb.dna.datalakehouse.dto.TablesCollectionResponseDto;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
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

	@Autowired
	private TrinoTableUtility tableUtility;
	
	private static String createSchema = "CREATE SCHEMA IF NOT EXISTS ";
	
	public BaseTrinoDataLakeService() {
		super();
	}
	
	@Autowired
	private TrinoConnectorRepo connectorRepo;
	
	@Transactional
	public List<String> connectorSpecificDataTypes(String connector){
		Optional<TrinoConnectorNsql> connectorDetails = connectorRepo.findById(connector);
		if(connectorDetails!=null && connectorDetails.get()!=null && connectorDetails.get().getData()!=null && connectorDetails.get().getData().getDataTypes()!=null) {
			return connectorDetails.get().getData().getDataTypes();
		}
		return null;
	}
	
	@Override
	@Transactional
	public Boolean isBucketExists(String bucketName) {
		return storageClient.isBucketExists(bucketName);
	}

	@Override
	@Transactional
	public TrinoDataLakeProjectResponseVO createDatalake(TrinoDataLakeProjectVO vo) throws Exception{
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO datalakeProjectVO  =  vo;
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		responseMsg.setErrors(errors);
		responseMsg.setWarnings(warnings);
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
			String schema = vo.getSchemaName();
			String catalog = vo.getCatalogName();
			String connectorType = vo.getConnectorType();
			String externalLocation = "s3a://"+vo.getBucketName()+"/"+ schema;
			String createSchemaStatement = createSchema + catalog + "." + schema + " WITH (location = '" + externalLocation + "')";
			try {
				try {
					trinoClient.executeStatments(createSchemaStatement);
					log.info("Successfully created Schema named {} at catalog {} at datalake project creation", schema, catalog);
				}catch(Exception e) {
					log.error("Failed while executing create schema statement {} at datalake project creation, with exception {}", createSchemaStatement, e.getMessage());
					MessageDescription err = new MessageDescription("Error while executing create schema " + schema + " statement at Datalake project creation.");
					errors.add(err);
					responseMsg.setErrors(errors);
					responseMsg.setWarnings(warnings);
					responseVO.setData(datalakeProjectVO);
					responseVO.setResponse(responseMsg);
					return responseVO;
				}
				if(vo.getTables()!=null && !vo.getTables().isEmpty()) {
					TablesCollectionResponseDto createdTablesResponse = this.createTables(connectorType, catalog, schema, externalLocation, vo.getTables());
					GenericMessage createResponses = createdTablesResponse.getResponse();
					vo.setTables(createdTablesResponse.getTables());
					responseMsg.getErrors().addAll(createResponses.getErrors());
					responseMsg.getWarnings().addAll(createResponses.getWarnings());
				}
				datalakeProjectVO = super.create(vo);
				responseVO.setData(datalakeProjectVO);
				responseMsg.setSuccess("SUCCESS");
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				responseVO.setResponse(responseMsg);
				return responseVO;
			}catch(Exception e) {
				e.printStackTrace();
				log.error("Failed at datalake project creation, with exception {}", e.getMessage());
				MessageDescription err = new MessageDescription("Error at Datalake project creation.");
				errors.add(err);
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				responseVO.setData(datalakeProjectVO);
				responseVO.setResponse(responseMsg);
				return responseVO;
			}
		}else {
			log.error("Failed while creating bucket {}  for Datalake project, to store metadata. Create Datalake Project Failed.", vo.getBucketName());
			MessageDescription err = new MessageDescription("Failed while creating bucket" + vo.getBucketName() + " for Datalake project, to store metadata. Create Datalake Project Failed.");
			errors.add(err);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseVO.setData(datalakeProjectVO);
			responseVO.setResponse(responseMsg);
			return responseVO;
		}
	}

	@Transactional
	public TablesCollectionResponseDto createTables(String connectorType, String catalog, String schema, String externalLocationForSchema, List<DatalakeTableVO> createTablesVO) {
		TablesCollectionResponseDto responseDto = new TablesCollectionResponseDto();
		List<DatalakeTableVO> successfullyCreatedTables = new ArrayList<>();
		GenericMessage responseMsg = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		List<String> dataTypes = this.connectorSpecificDataTypes(connectorType); 
			for(DatalakeTableVO tableVO : createTablesVO) {
				GenerateTableStmtResponseVO generateCreateStmtResponse = tableUtility.generateCreateTableStatement(dataTypes, catalog, schema, externalLocationForSchema, tableVO);
				if(generateCreateStmtResponse!= null) {
					if(generateCreateStmtResponse.getTableStmt()!=null && !"".equalsIgnoreCase(generateCreateStmtResponse.getTableStmt())) {
						try {
							trinoClient.executeStatments(generateCreateStmtResponse.getTableStmt());
							successfullyCreatedTables.add(generateCreateStmtResponse.getGeneratedTable());
						}catch(Exception e) {
							MessageDescription errMsg = new MessageDescription("Failed to execute create table statement for table " + tableVO.getTableName() + " with exception " + e.getMessage());
							errors.add(errMsg);
						}
					}
					warnings.addAll(generateCreateStmtResponse.getResponseMsg().getWarnings());
				}
			}
		responseMsg.setErrors(errors);
		responseMsg.setWarnings(warnings);
		responseDto.setTables(successfullyCreatedTables);
		responseDto.setResponse(responseMsg);
		return responseDto;
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
