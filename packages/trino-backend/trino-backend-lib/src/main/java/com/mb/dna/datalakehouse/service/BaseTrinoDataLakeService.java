package com.mb.dna.datalakehouse.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.application.client.DataProductClient;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.TrinoDataLakeAssembler;
import com.daimler.data.assembler.TrinoTableUtility;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dna.trino.config.KubernetesClient;
import com.daimler.data.dna.trino.config.TrinoClient;
import com.daimler.data.dto.dataproduct.DataProductCollection;
import com.daimler.data.dto.storage.CreateBucketResponseWrapperDto;
import com.daimler.data.dto.storage.DeleteBucketResponseWrapperDto;
import com.daimler.data.dto.storage.UpdateBucketResponseWrapperDto;
import com.daimler.data.service.common.BaseCommonService;
import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.db.entities.TrinoConnectorNsql;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoAccess;
import com.mb.dna.datalakehouse.db.jsonb.TrinoSchemaRules;
import com.mb.dna.datalakehouse.db.jsonb.TrinoTableRules;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoAccessRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoConnectorRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeCustomRepo;
import com.mb.dna.datalakehouse.db.repo.TrinoDataLakeRepo;
import com.mb.dna.datalakehouse.dto.DataLakeTableCollabDetailsVO;
import com.mb.dna.datalakehouse.dto.DataLakeTableColumnDetailsVO;
import com.mb.dna.datalakehouse.dto.DataProductDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
import com.mb.dna.datalakehouse.dto.GenerateTableStmtResponseVO;
import com.mb.dna.datalakehouse.dto.TablesCollectionResponseDto;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectUpdateRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

import io.kubernetes.client.openapi.ApiException;
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
	private TrinoAccessCustomRepo accessCustomRepo;
	
	@Autowired
	private TrinoAccessRepo accessJpaRepo;
	
	@Autowired
	private TrinoDataLakeAssembler assembler;
	
	@Autowired
	private StorageServicesClient storageClient;
	
	@Autowired
	private TrinoClient trinoClient;

	@Autowired
	private KubernetesClient kubeClient;
	
	@Autowired
	private TrinoTableUtility tableUtility;
	
	@Autowired
	private DataProductClient dataProductClient;
	
	private static String createSchema = "CREATE SCHEMA IF NOT EXISTS ";
	
	private static List<String> readPrivileges = Arrays.asList(new String[] {"SELECT"});
	private static List<String> writePrivileges = Arrays.asList(new String[] {"SELECT","INSERT","DELETE","UPDATE"});
	private static List<String> ownerShipPrivileges = Arrays.asList(new String[] {"SELECT","INSERT","DELETE","UPDATE","OWNERSHIP"});
	
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
		List<DataLakeTableCollabDetailsVO> collaborators = new ArrayList<>();
		List<String> schemaCollaborators = new ArrayList<>();
		DataLakeTableCollabDetailsVO ownerAsCollab = new DataLakeTableCollabDetailsVO();
		ownerAsCollab.setCollaborator(vo.getCreatedBy());
		ownerAsCollab.setHasWritePermission(true);
		collaborators.add(ownerAsCollab);
		schemaCollaborators.add(vo.getCreatedBy().getId());
		List<String> ownershipCollabs = new ArrayList<>();
		List<String> readCollabs = new ArrayList<>();
		List<String> writeCollabs = new ArrayList<>();
		ownershipCollabs.add(vo.getCreatedBy().getId());
		if(vo.getTechUserClientId()!=null) {
			ownershipCollabs.add(vo.getTechUserClientId());
		}
		if(vo.getCollabs()!=null && !vo.getCollabs().isEmpty()) {
			for(DataLakeTableCollabDetailsVO collab : vo.getCollabs()) {
				collaborators.add(collab);
				schemaCollaborators.add(collab.getCollaborator().getId());
				if(collab.getHasWritePermission()!=null && collab.getHasWritePermission()) {
					writeCollabs.add(collab.getCollaborator().getId());
				}else {
					readCollabs.add(collab.getCollaborator().getId());
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
			String createSchemaStatement = createSchema + catalog + "." + schema 
					+ " WITH (location = '" + externalLocation + "')";
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
					TablesCollectionResponseDto createdTablesResponse = this.createTables(connectorType, catalog, schema, externalLocation,null, vo.getTables(), null);
					GenericMessage createResponses = createdTablesResponse.getResponse();
					vo.setTables(createdTablesResponse.getTables());
					if(createdTablesResponse.getTables() !=null && !createdTablesResponse.getTables().isEmpty()) {
						TrinoAccessNsql accessNsql = null;
						TrinoAccess accessRules = null;
						TrinoAccess updatedAccessRules = null;
						try{
							List<TrinoAccessNsql> accessRecords = accessJpaRepo.findAll();
							if(accessRecords!=null && !accessRecords.isEmpty() && accessRecords.get(0)!=null && accessRecords.get(0).getData()!=null) {
								accessNsql = accessRecords.get(0);
								accessRules = accessRecords.get(0).getData();
								updatedAccessRules = accessRecords.get(0).getData();
							}
							if(updatedAccessRules!=null) {
								TrinoSchemaRules schemaRules = new TrinoSchemaRules();
								schemaRules.setCatalog(catalog);
								schemaRules.setOwner(false);
								schemaRules.setSchema(schema);
								schemaRules.setUser(String.join("|", schemaCollaborators));
								updatedAccessRules.getSchemas().add(schemaRules);
								
								//remove any existing table rules for new project
								updatedAccessRules.getTables().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
										&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
								
								TrinoTableRules readAccessRules = new TrinoTableRules();
								readAccessRules.setCatalog(catalog);
								readAccessRules.setPrivileges(readPrivileges);
								readAccessRules.setSchema(schema);
								readAccessRules.setTable(".*");
								readAccessRules.setUser(String.join("|", readCollabs));
								if(readCollabs!=null && !readCollabs.isEmpty())
									updatedAccessRules.getTables().add(readAccessRules);
								
								List<String> ownershipUsers = new ArrayList<>();
								ownershipUsers.addAll(ownershipCollabs);
								ownershipUsers.addAll(writeCollabs);
								TrinoTableRules techUserTableRule = new TrinoTableRules();
								techUserTableRule.setCatalog(catalog);
								techUserTableRule.setSchema(schema);
								techUserTableRule.setUser(String.join("|", ownershipUsers));
								techUserTableRule.setTable(".*");
								techUserTableRule.setPrivileges(ownerShipPrivileges);
								updatedAccessRules.getTables().add(techUserTableRule);
								
								accessNsql.setData(updatedAccessRules);
								accessJpaRepo.save(accessNsql);
							}
							
						}catch(Exception e) {
							log.error("Failed at fetching accessrules from database for updating collaborators and table mappings. Exception {}", e.getMessage());
							MessageDescription warning = new MessageDescription("Failed to update access rules, unable to fetch current access rules record.");
							warnings.add(warning);
						}
						
					}
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
	public TablesCollectionResponseDto createTables(String connectorType, String catalog, String schema, String externalLocationForSchema,List<String> existingTablesInDB, List<DatalakeTableVO> createRequestTablesVO, List<String> existingTablesInSchema) {
		TablesCollectionResponseDto responseDto = new TablesCollectionResponseDto();
		List<DatalakeTableVO> successfullyCreatedTables = new ArrayList<>();
		GenericMessage responseMsg = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		List<String> dataTypes = this.connectorSpecificDataTypes(connectorType); 
			for(DatalakeTableVO tableVO : createRequestTablesVO) {
				if(existingTablesInSchema==null || (existingTablesInSchema!=null && existingTablesInSchema.isEmpty()) || (existingTablesInSchema!=null && !existingTablesInSchema.isEmpty() && !existingTablesInSchema.contains(tableVO.getTableName()))) {
					if(existingTablesInDB==null || existingTablesInDB.isEmpty() ||  !existingTablesInDB.contains(tableVO.getTableName())) {	
						GenerateTableStmtResponseVO generateCreateStmtResponse = tableUtility.generateCreateTableStatement(dataTypes, catalog, schema, externalLocationForSchema, tableVO);
						if(generateCreateStmtResponse!= null) {
							if(generateCreateStmtResponse.getTableStmt()!=null && !"".equalsIgnoreCase(generateCreateStmtResponse.getTableStmt())) {
								try {
									trinoClient.executeStatments( generateCreateStmtResponse.getTableStmt());
									successfullyCreatedTables.add(generateCreateStmtResponse.getGeneratedTable());
								}catch(Exception e) {
									MessageDescription errMsg = new MessageDescription("Failed to execute create table statement for table " + tableVO.getTableName() + " with exception " + e.getMessage());
									warnings.add(errMsg);
								}
							}
							warnings.addAll(generateCreateStmtResponse.getResponseMsg().getWarnings());
						}
					}
				}else {
					successfullyCreatedTables.add(tableVO);
				}
			}
			if(existingTablesInSchema!=null && !existingTablesInSchema.isEmpty()) {
				List<String> successfullyCreatedTablesString = new ArrayList<>();
				successfullyCreatedTablesString = successfullyCreatedTables.stream().map(n->n.getTableName()).collect(Collectors.toList());
				for(String schemaTable : existingTablesInSchema) {
					if(!successfullyCreatedTablesString.contains(schemaTable) && (existingTablesInDB==null || existingTablesInDB.isEmpty() || !existingTablesInDB.contains(schemaTable))) {
						DatalakeTableVO schemaTableVO = new DatalakeTableVO();
						schemaTableVO.setDescription("");
						schemaTableVO.setTableName(schemaTable);
						schemaTableVO.setExternalLocation("");
						schemaTableVO.setDataFormat("");
						schemaTableVO.setXCoOrdinate(new BigDecimal("0"));
						schemaTableVO.setYCoOrdinate(new BigDecimal("0")); 
						List<DataLakeTableColumnDetailsVO> columns = new ArrayList<>();
						columns = trinoClient.showColumns(catalog, schema, schemaTable);
						schemaTableVO.setColumns(columns);
						successfullyCreatedTables.add(schemaTableVO);
					}else {
						if(existingTablesInDB!=null && !existingTablesInDB.isEmpty() && existingTablesInDB.contains(schemaTable)) {
							try {
								trinoClient.executeStatments("DROP TABLE IF EXISTS " + catalog + "." + schema + "." + schemaTable);
							}catch(Exception e) {
								log.error("Failed while dropping table {} under schema {} . Caused due to Exception {}", schemaTable, schema, e.getMessage());
								MessageDescription msg = new MessageDescription("Failed to drop table " + schemaTable + ", retry deleting");
								warnings.add(msg);
							}
						}
					}
				}
			}
		responseMsg.setErrors(errors);
		responseMsg.setWarnings(warnings);
		responseDto.setTables(successfullyCreatedTables);
		responseDto.setResponse(responseMsg);
		return responseDto;
	}
	
	@Override
	@Transactional
	public TrinoDataLakeProjectResponseVO updateDatalake(TrinoDataLakeProjectVO existingVO,TrinoDataLakeProjectUpdateRequestVO updateRequestVO) throws Exception{
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO datalakeProjectVO  =  existingVO;
		
		existingVO.setDescription(updateRequestVO.getDescription());
		existingVO.setClassificationType(updateRequestVO.getClassificationType());
		existingVO.setHasPii(updateRequestVO.getHasPii());
		existingVO.setDivisionId(updateRequestVO.getDivisionId());
		existingVO.setDivisionName(updateRequestVO.getDivisionName());
		existingVO.setSubdivisionId(updateRequestVO.getSubdivisionId());
		existingVO.setSubdivisionName(updateRequestVO.getSubdivisionName());
		existingVO.setDepartment(updateRequestVO.getDepartment());
		existingVO.setCollabs(updateRequestVO.getCollabs());
		
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		responseMsg.setErrors(errors);
		responseMsg.setWarnings(warnings);
		List<DataLakeTableCollabDetailsVO> collaborators = new ArrayList<>();
		List<String> schemaCollaborators = new ArrayList<>();
		DataLakeTableCollabDetailsVO ownerAsCollab = new DataLakeTableCollabDetailsVO();
		ownerAsCollab.setCollaborator(existingVO.getCreatedBy());
		ownerAsCollab.setHasWritePermission(true);
		collaborators.add(ownerAsCollab);
		schemaCollaborators.add(existingVO.getCreatedBy().getId());
		List<String> ownershipCollabs = new ArrayList<>();
		List<String> readCollabs = new ArrayList<>();
		List<String> writeCollabs = new ArrayList<>();
		ownershipCollabs.add(existingVO.getCreatedBy().getId());
		if(existingVO.getTechUserClientId()!=null) {
			ownershipCollabs.add(existingVO.getTechUserClientId());
		}
		if(existingVO.getCollabs()!=null && !existingVO.getCollabs().isEmpty()) {
			for(DataLakeTableCollabDetailsVO collab : existingVO.getCollabs()) {
				collaborators.add(collab);
				schemaCollaborators.add(collab.getCollaborator().getId());
				if(collab.getHasWritePermission()!=null && collab.getHasWritePermission()) {
					writeCollabs.add(collab.getCollaborator().getId());
				}else {
					readCollabs.add(collab.getCollaborator().getId());
				}
			}
		}
		UpdateBucketResponseWrapperDto updateBucketResponse = storageClient.updateBucket(existingVO.getBucketName(), existingVO.getBucketId(), collaborators);
		if (updateBucketResponse.getErrors() != null) {
			log.error("Failed while saving details of collaborator for storage bucket {} Caused due to Exception {}", existingVO.getBucketName(), updateBucketResponse.getErrors().get(0).getMessage());
			MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
			errors.add(msg);
			responseMsg.setSuccess("FAILED");
			responseMsg.setErrors(errors);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
			responseVO.setData(datalakeProjectVO);
			responseVO.setResponse(responseMsg);
			return responseVO;
		}
		else {
			log.info("Bucket {} has been successfully updated with latest collaborators", existingVO.getBucketName());
			String schema = existingVO.getSchemaName();
			String catalog = existingVO.getCatalogName();
			String connectorType = existingVO.getConnectorType();
			String externalLocation = "s3a://"+existingVO.getBucketName()+"/"+ schema;
			try {
				if(updateRequestVO.getTables()!=null && !updateRequestVO.getTables().isEmpty()) {
					List<String> existingTablesInSchema = trinoClient.showTables(catalog, schema, "%%");
					List<String> existingTablesInDna = new ArrayList<>();
					if(existingVO.getTables()!=null && !existingVO.getTables().isEmpty()) {
						existingTablesInDna = existingVO.getTables().stream().map(x->x.getTableName()).collect(Collectors.toList()); 
					}
//					if()
					List<String> tablesForDrop = new ArrayList<>();
					TablesCollectionResponseDto createdTablesResponse = this.createTables(connectorType, catalog, schema, externalLocation,existingTablesInDna, updateRequestVO.getTables(), existingTablesInSchema);
					GenericMessage createResponses = createdTablesResponse.getResponse();
					existingVO.setTables(createdTablesResponse.getTables());
					if(createdTablesResponse.getTables() !=null && !createdTablesResponse.getTables().isEmpty()) {
						TrinoAccessNsql accessNsql = null;
						TrinoAccess accessRules = null;
						TrinoAccess updatedAccessRules = null;
						try{
							List<TrinoAccessNsql> accessRecords = accessJpaRepo.findAll();
							if(accessRecords!=null && !accessRecords.isEmpty() && accessRecords.get(0)!=null && accessRecords.get(0).getData()!=null) {
								accessNsql = accessRecords.get(0);
								accessRules = accessRecords.get(0).getData();
								updatedAccessRules = accessRecords.get(0).getData();
							}
							if(updatedAccessRules!=null) {
								
								//remove existing schema rules
								updatedAccessRules.getSchemas().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog) 
										&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
								
								//added new schema rules
								TrinoSchemaRules schemaRules = new TrinoSchemaRules();
								schemaRules.setCatalog(catalog);
								schemaRules.setOwner(false);
								schemaRules.setSchema(schema);
								schemaRules.setUser(String.join("|", schemaCollaborators));
								updatedAccessRules.getSchemas().add(schemaRules);
								
								//remove existing table rules
								updatedAccessRules.getTables().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
										&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
								
								//adding new table rules
								TrinoTableRules readAccessRules = new TrinoTableRules();
								readAccessRules.setCatalog(catalog);
								readAccessRules.setPrivileges(readPrivileges);
								readAccessRules.setSchema(schema);
								readAccessRules.setTable(".*");
								readAccessRules.setUser(String.join("|", readCollabs));
								if(readCollabs!=null && !readCollabs.isEmpty())
									updatedAccessRules.getTables().add(readAccessRules);
								
								List<String> ownershipUsers = new ArrayList<>();
								ownershipUsers.addAll(writeCollabs);
								ownershipUsers.addAll(ownershipCollabs);
								TrinoTableRules techUserTableRule = new TrinoTableRules();
								techUserTableRule.setCatalog(catalog);
								techUserTableRule.setSchema(schema);
								techUserTableRule.setUser(String.join("|", ownershipUsers));
								techUserTableRule.setTable(".*");
								techUserTableRule.setPrivileges(ownerShipPrivileges);
								updatedAccessRules.getTables().add(techUserTableRule);
								
								accessNsql.setData(updatedAccessRules);
								accessJpaRepo.save(accessNsql);
							}
						}catch(Exception e) {
							log.error("Failed at managing accessrules from database for updating collaborators and table mappings. Exception {}", e.getMessage());
							MessageDescription warning = new MessageDescription("Failed to update access rules.");
							warnings.add(warning);
						}
						
					}
					responseMsg.getErrors().addAll(createResponses.getErrors());
					responseMsg.getWarnings().addAll(createResponses.getWarnings());
				}
				datalakeProjectVO = super.create(existingVO);
				responseVO.setData(datalakeProjectVO);
				responseMsg.setSuccess("SUCCESS");
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				responseVO.setResponse(responseMsg);
				return responseVO;
			}catch(Exception e) {
				log.error("Failed at datalake project updation, with exception {}", e.getMessage());
				MessageDescription err = new MessageDescription("Error at Datalake project updation.");
				errors.add(err);
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
				responseVO.setData(datalakeProjectVO);
				responseVO.setResponse(responseMsg);
				return responseVO;
			}
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

	
	@Override
	@Transactional
	public TrinoDataLakeProjectVO getUpdatedById(String id) {
		TrinoDataLakeProjectVO existingVO = super.getById(id);
		List<DatalakeTableVO> existingTablesVO = existingVO.getTables();
		String catalog = existingVO.getCatalogName();
		String schema = existingVO.getSchemaName();
		List<String> latestTables = trinoClient.showTables(existingVO.getCatalogName(), existingVO.getSchemaName(), "%%");
		List<String> existingTablesInDNA = existingVO.getTables().stream().map(n->n.getTableName()).collect(Collectors.toList());
		if(latestTables!=null && !latestTables.isEmpty()) {
			for(String table : latestTables) {
				if(!existingTablesInDNA.contains(table)) {
					DatalakeTableVO newTable = new DatalakeTableVO();
					newTable.setDescription("");
					newTable.setTableName(table);
					newTable.setExternalLocation("");
					newTable.setDataFormat("");
					newTable.setXCoOrdinate(new BigDecimal("0"));
					newTable.setYCoOrdinate(new BigDecimal("0")); 
					List<DataLakeTableColumnDetailsVO> columns = new ArrayList<>();
					columns = trinoClient.showColumns(existingVO.getCatalogName(), existingVO.getSchemaName(), table);
					newTable.setColumns(columns);
					existingTablesVO.add(newTable);
				}
			}
		}
		List<DatalakeTableVO> updatedTablesVO = existingTablesVO;
		Boolean tablesDeletedFromOutsideDna = existingVO.getDataProductDetails().getInvalidState();
		for(DatalakeTableVO vo : existingTablesVO) {
			if((latestTables!=null && !latestTables.isEmpty() && !latestTables.contains(vo.getTableName())) || latestTables == null || latestTables.isEmpty()) {
				updatedTablesVO.remove(vo);
				tablesDeletedFromOutsideDna = true;
				try {
					trinoClient.executeStatments("DROP TABLE IF EXISTS " + catalog + "." + schema + "." + vo.getTableName());
					log.info("Removed table {} from catalog {} and schema {} during get operation while syncing", vo.getTableName(), catalog, schema);
				}catch(Exception e) {
					log.error("Failed while dropping table {} under catalog {} schema {} . Caused due to Exception {}", vo.getTableName(),catalog, schema, e.getMessage());
				}
			}
		}
		existingVO.getDataProductDetails().setInvalidState(tablesDeletedFromOutsideDna);
		existingVO.setTables(updatedTablesVO);
		TrinoDataLakeProjectVO updatedVO = super.create(existingVO);
		return updatedVO;
	}

	@Override
	@Transactional
	public Boolean isKeyExists(String key,String projectName) throws ApiException {
		Long existingProjectsWithSameKey =  customRepo.getCountOfExistingProjectsWithSameKey(key,projectName);
		if(existingProjectsWithSameKey>0) {
			return true;
		}
		return kubeClient.isKeyExists(key);
	}
	
	@Override
	@Transactional
	public DataProductDetailsVO isValidDataProduct(String id) throws Exception {
		DataProductDetailsVO details = new DataProductDetailsVO();
		DataProductCollection collection = dataProductClient.getMyDataProducts();
		if(collection==null || collection.getRecords()==null) {
			return null;
		}else {
			if(!collection.getRecords().isEmpty()) {
				for(DataProductDetailsVO vo : collection.getRecords() ) {
					if(vo.getId()!=null && vo.getId().equals(id)) {
						details = vo;
						break;
					}
				}
			}	
			return details;
		}
	}
	
	@Override
	@Transactional
	public GenericMessage updateTechUserDetails(TrinoDataLakeProjectVO existingProject, String clientId, String clientSecret) {
		GenericMessage responseMsg = new GenericMessage();
		responseMsg.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		TrinoAccessNsql accessNsql = null;
		TrinoAccess accessRules = null;
		TrinoAccess updatedAccessRules = null;
		String catalog = existingProject.getCatalogName();
		String schema = existingProject.getSchemaName();
		String existingSchemaUsers = "";
		String updatedSchemaUsers = "";
		boolean updateTechUser = false;
		String existingClientId = "";
		String operation = "add";
		if(existingProject.getTechUserClientId()!=null && !"".equalsIgnoreCase(existingProject.getTechUserClientId())) {
			updateTechUser = true;
			existingClientId = existingProject.getTechUserClientId();
		}
		try{
			List<TrinoAccessNsql> accessRecords = accessJpaRepo.findAll();
			if(accessRecords!=null && !accessRecords.isEmpty() && accessRecords.get(0)!=null && accessRecords.get(0).getData()!=null) {
				accessNsql = accessRecords.get(0);
				accessRules = accessRecords.get(0).getData();
				updatedAccessRules = accessRecords.get(0).getData();
			}
			if(updatedAccessRules!=null) {
				TrinoSchemaRules schemaRules = new TrinoSchemaRules();
				schemaRules.setCatalog(catalog);
				schemaRules.setOwner(false);
				schemaRules.setSchema(schema);
				if(!updateTechUser) {
					updatedSchemaUsers = existingSchemaUsers + "|"+ clientId;
				}else {
					operation = "replace";
					updatedSchemaUsers = existingSchemaUsers.replaceAll(existingClientId, clientId);
					updatedAccessRules.getTables().removeIf(x->x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
															&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema)
															&& x.getPrivileges().contains("OWNERSHIP"));
				}
				try {
					kubeClient.operateRecordToConfigMap(operation,existingClientId, clientId, clientSecret);
					log.info("Details of technical user is updated successfully at kubernetes passwords file for project {} ",existingProject.getProjectName());
				}catch(ApiException e) {
					log.error("Failed at updating techUser access rules in kubernetes config. Exception {}", e.getMessage());
					MessageDescription warning = new MessageDescription("Failed to update Tech User access rules, Internal Server error. Please retry after a while.");
					warnings.add(warning);
					responseMsg.setErrors(errors);
					responseMsg.setWarnings(warnings);
					return responseMsg;
				}
				
				schemaRules.setUser(updatedSchemaUsers);
				updatedAccessRules.getSchemas().removeIf(x->x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
															&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
				updatedAccessRules.getSchemas().add(schemaRules);
				
				List<String> ownershipUsers = new ArrayList<>();
				ownershipUsers.add(existingProject.getCreatedBy().getId());
				ownershipUsers.add(clientId);
				List<DataLakeTableCollabDetailsVO> collabs = existingProject.getCollabs();
				if(collabs!=null && !collabs.isEmpty()) {
					for(DataLakeTableCollabDetailsVO collab : collabs) {
						if(collab.getHasWritePermission()!=null && collab.getHasWritePermission()) {
							ownershipUsers.add(collab.getCollaborator().getId());
						}
					}
				}
				TrinoTableRules techUserTableRule = new TrinoTableRules();
				techUserTableRule.setCatalog(catalog);
				techUserTableRule.setSchema(schema);
				techUserTableRule.setUser(String.join("|", ownershipUsers));
				techUserTableRule.setTable(".*");
				techUserTableRule.setPrivileges(ownerShipPrivileges);
				updatedAccessRules.getTables().add(techUserTableRule);
				accessNsql.setData(updatedAccessRules);
				accessJpaRepo.save(accessNsql);
				existingProject.setTechUserClientId(clientId);
				this.create(existingProject);
				responseMsg.setSuccess("SUCCESS");
				responseMsg.setErrors(errors);
				responseMsg.setWarnings(warnings);
			}
		}catch(Exception e) {
			log.error("Failed at fetching accessrules from database for updating techUser access rules. Exception {}", e.getMessage());
			MessageDescription warning = new MessageDescription("Failed to update Tech User access rules, unable to fetch current access rules record.");
			warnings.add(warning);
			responseMsg.setErrors(errors);
			responseMsg.setWarnings(warnings);
		}
		return responseMsg;
	}

	@Override
	public GenericMessage deleteProjectById(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		TrinoAccessNsql accessNsql = null;
		TrinoAccess accessRules = null;
		TrinoAccess updatedAccessRules = null;
		TrinoDataLakeProjectVO existingVO = super.getById(id);
		String schema = existingVO.getSchemaName();
		String catalog = existingVO.getCatalogName();
		String bucketName = existingVO.getBucketName();
		try {
			//deleted record from db
			jpaRepo.deleteById(id);
			log.info("Deleted datalake record from db successfully");
			List<TrinoAccessNsql> accessRecords = accessJpaRepo.findAll();
			if(accessRecords!=null && !accessRecords.isEmpty() && accessRecords.get(0)!=null && accessRecords.get(0).getData()!=null) {
				accessNsql = accessRecords.get(0);
				accessRules = accessRecords.get(0).getData();
				updatedAccessRules = accessRecords.get(0).getData();
			}
			//removing existing schema
			updatedAccessRules.getSchemas().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog) 
					&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
			log.info("Removed schema rule of {} for record during deletion", schema);
			//remove existing table rules
			updatedAccessRules.getTables().removeIf(x-> x.getCatalog()!=null && x.getCatalog().equalsIgnoreCase(catalog)  
					&& x.getSchema()!=null && x.getSchema().equalsIgnoreCase(schema));
			log.info("Removed all existing table rules including tech user table rule if exists, of record during deletion for schema {} ",schema);
			accessNsql.setData(updatedAccessRules);
			accessJpaRepo.save(accessNsql);
			
		}catch(Exception e) {
			MessageDescription error = new MessageDescription("Failed to delete the record with exception, please try again later");
			log.error("Failed to delete record with id {} catalog {} schema {} with exception {}",id,catalog,schema,e.getMessage());
			errors.add(error);
			responseMessage.setSuccess("FAILED");
			responseMessage.setErrors(errors);
			responseMessage.setWarnings(warnings);
			return responseMessage;
		}
		// dropping schema 
		try {
			trinoClient.executeStatments("DROP SCHEMA IF EXISTS " + catalog + "." + schema );
		}catch(Exception e) {
			MessageDescription warning = new MessageDescription("Failed to drop schema while deleting datalake, Will be automatically dropped during daily cleanup.");
			log.error("Failed to drop schema while deleting datalake with id {} catalog {} schema {} with exception {}",id,catalog,schema,e.getMessage());
			warnings.add(warning);
		}
		//deleting bucket
		DeleteBucketResponseWrapperDto response = storageClient.deleteBucket(bucketName);
		if (response != null && "FAILED".equalsIgnoreCase(response.getStatus())) {
			MessageDescription warning = new MessageDescription("Failed to delete storage bucket while deleting datalake, Will be automatically delete during daily cleanup.");
			warnings.add(warning);
		}
		responseMessage.setSuccess("SUCCESS");
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}
	
	
	
}
