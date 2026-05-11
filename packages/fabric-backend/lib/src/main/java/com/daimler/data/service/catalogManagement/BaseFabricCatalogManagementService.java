package com.daimler.data.service.catalogManagement;

import com.daimler.data.application.client.FabricCDCPushServiceClient;
import com.daimler.data.application.client.GenesisApiClient;
import com.daimler.data.application.client.FabricWorkspaceClient;
import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.application.client.UiLiciousClient;
import com.daimler.data.assembler.FabricCatalogMetadataAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.assembler.DdxDataProductsDetailsAssembler;
import com.daimler.data.controller.exceptions.*;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.entities.DdxDataProductsDetailsNsql;
import com.daimler.data.db.json.DdxDataProductsDetail;
import com.daimler.data.db.json.DdxGroupDetail;
import com.daimler.data.db.json.DdxMirroredCatalogProduct;
import com.daimler.data.db.json.DdxProduct;
import com.daimler.data.db.json.Fabric2FabricDetail;
import com.daimler.data.db.json.GroupNameDetail;
import com.daimler.data.db.json.GroupNameList;
import com.daimler.data.db.json.MirroredCatalogDetail;
import com.daimler.data.db.json.MirroredObjectDetail;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.entities.DdxMirroredCatalogProductNsql;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementCustomRepository;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementRepository;
import com.daimler.data.db.repo.ddxDataProductsDetails.DdxDataProductsDetailsRepository;
import com.daimler.data.db.repo.ddxMirroredCatalogProduct.DdxMirroredCatalogProductCustomRepository;
import com.daimler.data.db.repo.ddxMirroredCatalogProduct.DdxMirroredCatalogProductRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabricWorkspace.FabricLakehouseVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.GroupDetailsVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameDetailVO;
import com.daimler.data.dto.fabricWorkspace.GroupNameListVO;
import com.daimler.data.dto.fabricWorkspace.CdcPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.DdxPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.Fabric2FabricDetailVO;
import com.daimler.data.dto.fabric.LegalEntityDto;
import com.daimler.data.dto.fabric.AddGroupDto;
import com.daimler.data.dto.fabricCatalogManagement.*;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.util.OpenMetadataFqnBuilder;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
@Transactional
public class BaseFabricCatalogManagementService extends BaseCommonService<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql, String> 
    implements FabricCatalogManagementService {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";
    private static final String CONFLICT_STATUS = "CONFLICT";
    private static final String NOT_FOUND_STATUS = "NOT_FOUND";

    @Value("${uilicious.email}")
    private String pidUser;

    @Value("${uilicious.identifier}")
    private String pidUserIdentifier; 

    private final FabricWorkspaceCustomRepository customRepo;
    private final FabricWorkspaceRepository jpaRepo;
    private final FabricWorkspaceAssembler assembler;
    private final OpenMetadataClient openMetadataClient;
    private final FabricCatalogManagementRepository catalogRepo;
    private final FabricCatalogManagementCustomRepository catalogCustomRepo;
    private final FabricCatalogMetadataAssembler catalogAssembler;
    private final FabricCDCPushServiceClient cdcPushServiceClient;
    private final GenesisApiClient genesisApiClient;
    private final FabricWorkspaceClient fabricWorkspaceClient;
    private final DdxDataProductsDetailsRepository ddxDataProductsDetailsRepository;
    private final DdxDataProductsDetailsAssembler ddxDataProductsDetailsAssembler;
    private final UiLiciousClient uiLiciousClient;
    private final DdxMirroredCatalogProductRepository mirroredCatalogRepo;
    private final DdxMirroredCatalogProductCustomRepository mirroredCatalogCustomRepo;

    @Value("${mirroredCatalog.central.workspaceId:}")
    private String centralWorkspaceId;

    @Value("${mirroredCatalog.central.workspaceName:}")
    private String centralWorkspaceName;

    @Autowired
    public BaseFabricCatalogManagementService(
            FabricWorkspaceCustomRepository customRepo,
            FabricWorkspaceRepository jpaRepo,
            FabricWorkspaceAssembler assembler,
            OpenMetadataClient openMetadataClient,
            FabricCatalogManagementRepository catalogRepo,
            FabricCatalogManagementCustomRepository catalogCustomRepo,
            FabricCatalogMetadataAssembler catalogAssembler,
            FabricCDCPushServiceClient cdcPushServiceClient,
            GenesisApiClient genesisApiClient,
            UiLiciousClient uiLiciousClient,
            FabricWorkspaceClient fabricWorkspaceClient,
            DdxDataProductsDetailsRepository ddxDataProductsDetailsRepository,
            DdxDataProductsDetailsAssembler ddxDataProductsDetailsAssembler,
            DdxMirroredCatalogProductRepository mirroredCatalogRepo,
            DdxMirroredCatalogProductCustomRepository mirroredCatalogCustomRepo) {
        this.customRepo = customRepo;
        this.jpaRepo = jpaRepo;
        this.assembler = assembler;
        this.openMetadataClient = openMetadataClient;
        this.catalogRepo = catalogRepo;
        this.catalogCustomRepo = catalogCustomRepo;
        this.catalogAssembler = catalogAssembler;
        this.cdcPushServiceClient = cdcPushServiceClient;
        this.genesisApiClient = genesisApiClient;
        this.fabricWorkspaceClient = fabricWorkspaceClient;
        this.ddxDataProductsDetailsRepository = ddxDataProductsDetailsRepository;
        this.ddxDataProductsDetailsAssembler = ddxDataProductsDetailsAssembler;
        this.uiLiciousClient = uiLiciousClient;
        this.mirroredCatalogRepo = mirroredCatalogRepo;
        this.mirroredCatalogCustomRepo = mirroredCatalogCustomRepo;
    }

    @Override
    @Transactional
    public PublishCatalogResponseVO publishCatalogMetaData(PublishCatalogRequestVO request, 
            FabricWorkspaceVO existingFabricWorkspace) {
        log.info("Publishing catalog metadata for workspace: {}", existingFabricWorkspace.getName());
        
        PublishCatalogResponseVO response = new PublishCatalogResponseVO();
        FabricCatalogMetadataDetailsVO catalogMetadataDetails = new FabricCatalogMetadataDetailsVO();
        
        try {
            // Validate and process owners
            List<EntityReference> ownerReferences = validateAndProcessOwners(request.getOwners(), response);
            if (ownerReferences.isEmpty()) {
                return response; 
            }

            // Process the catalog metadata
            processCatalogMetadata(request, existingFabricWorkspace, ownerReferences, catalogMetadataDetails);

            // Update CDC lake house details
            updateLakeHouseDetails(existingFabricWorkspace, request.getMetadata());

            // Save metadata to repository
            saveCatalogMetadata(request, catalogMetadataDetails);

            // Prepare success response
            prepareSuccessResponse(response, catalogMetadataDetails);

            // Trigger UiLicious service principal addition after successful CDC push
            triggerUiLiciousForLakehouses(existingFabricWorkspace);

        } catch (EntityAlreadyExistsException e) {
            log.error("Catalog metadata already exists for workspace: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(CONFLICT_STATUS,
                    "Catalog metadata already exists. Error: " + e.getMessage()));
        } catch (OpenMetadataClientException e) {
            log.error("Failed to publish catalog for workspace: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(FAILED_STATUS,
                    "Failed to publish catalog: " + e.getMessage()));
            openMetadataClient.deleteDatabaseService(existingFabricWorkspace.getName());
        } catch (Exception e) {
            log.error("Unexpected error publishing catalog for workspace: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(FAILED_STATUS,
                    "Failed to publish catalog: " + e.getMessage()));
            openMetadataClient.deleteDatabaseService(existingFabricWorkspace.getName());
        }

        return response;
    }

    @Override
    public PublishCatalogResponseVO getCatalogMetadata(String serviceName) {
        log.info("Fetching catalog metadata for service: {}", serviceName);
        
        PublishCatalogResponseVO response = new PublishCatalogResponseVO();
        
        try {
            // Retrieve metadata from OpenMetadata
            FabricCatalogMetadataVO metadata = retrieveMetadataFromOpenMetadata(serviceName);
            
            // Retrieve stored metadata details
            FabricCatalogMetadataDetailsVO catalogMetadataDetails = retrieveStoredMetadataDetails(serviceName, metadata);
            
            // Prepare success response
            prepareSuccessResponse(response, catalogMetadataDetails);
            
        } catch (EntityNotFoundException e) {
            log.error("Metadata not found for service: {}", serviceName, e);
            throw new EntityNotFoundException("Metadata details", serviceName);
        } catch (Exception e) {
            log.error("Failed to get catalog metadata for service: {}", serviceName, e);
            throw new OpenMetadataClientException("Failed to get catalog metadata for workspace: " + 
                serviceName + " " + e.getMessage(), e);
        }
        
        return response;
    }

    @Override
    @Transactional
    public PublishCatalogResponseVO updateCatalogMetaData(PublishCatalogRequestVO request, 
            FabricWorkspaceVO existingFabricWorkspace) {
        log.info("Updating catalog metadata for workspace: {}", existingFabricWorkspace.getName());
        
        PublishCatalogResponseVO response = new PublishCatalogResponseVO();
        
        try {
            // Validate and process owners
            List<EntityReference> ownerReferences = validateAndProcessOwners(request.getOwners(), response);
            if (ownerReferences.isEmpty()) {
                return response; // Response already contains error message
            }

            // Get existing metadata for comparison
            PublishCatalogResponseVO catalogDetails = getCatalogMetadata(existingFabricWorkspace.getName());
            FabricCatalogMetadataVO existingMetadata = catalogDetails.getData().getMetadata();
            
            // Get existing service
            DatabaseService service = openMetadataClient.getDatabaseService(existingFabricWorkspace.getName());
            
            // Update service owners if changed
            updateServiceOwners(service, ownerReferences);

            // Process deletions first (bottom-up: columns -> tables -> schemas -> databases)
            handleDeletions(existingMetadata, request.getMetadata());

            // Process updates and additions
            processUpdates(request, existingFabricWorkspace.getName(), ownerReferences);

            // Update CDC lake house details
            updateLakeHouseDetails(existingFabricWorkspace, request.getMetadata());

            // Update stored metadata
            updateStoredMetadata(request);

            // Prepare success response
			FabricCatalogMetadataNsql entity = catalogCustomRepo.findByServiceName(request.getMetadata().getServiceName())
            .orElseThrow(() -> new EntityNotFoundException("Catalog metadata", request.getMetadata().getServiceName()));
        	FabricCatalogMetadataDetailsVO vo = catalogAssembler.toVo(entity);
			response.setData(vo);
            response.setResponses(new GenericMessage(SUCCESS_STATUS));

            // Trigger UiLicious service principal addition after successful CDC push
            triggerUiLiciousForLakehouses(existingFabricWorkspace);

        } catch (EntityNotFoundException e) {
            log.error("Catalog metadata not found for service: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(NOT_FOUND_STATUS, 
                "Catalog metadata not found for service: " + existingFabricWorkspace.getName() + ". " + e.getMessage()));
        } catch (OpenMetadataClientException | EntityAlreadyExistsException e) {
            log.error("Failed to update catalog metadata for service: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(FAILED_STATUS, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating catalog metadata for service: {}", existingFabricWorkspace.getName(), e);
            response.setResponses(createErrorResponse(FAILED_STATUS, "Unexpected error: " + e.getMessage()));
        }
        
        return response;
    }

    @Override
    public List<LegalEntitiesResponseVO> getAllFabricLegalEntities(String queryString){

        List<LegalEntityDto> legalEntityCache = new ArrayList<>();
        legalEntityCache = this.genesisApiClient.getLegalEntities();
        return legalEntityCache.stream().filter(dto -> dto.getLegalName().toLowerCase().contains(queryString.toLowerCase()) ||
                    dto.getCompanyCode().contains(queryString)).map(dto -> this.genesisApiClient.createVoObject(dto)).toList();
    }

    private List<EntityReference> validateAndProcessOwners(List<CreatedByVO> owners, PublishCatalogResponseVO response) {
        List<EntityReference> ownerReferences = new ArrayList<>();
        List<MessageDescription> warningMessages = new ArrayList<>();
        
        for (CreatedByVO owner : new ArrayList<>(owners)) {
            try {
                User user = openMetadataClient.getUserByFqn(owner.getId());
                ownerReferences.add(openMetadataClient.createEntityReference(user));
            } catch (EntityNotFoundException e) {
                MessageDescription warning = new MessageDescription();
                warning.setMessage("User " + owner.getId() + 
                    " not found in OpenMetadata. Please ensure they've logged in to CDC.");
                warningMessages.add(warning);
                owners.remove(owner);
            }
        }

        if (ownerReferences.isEmpty()) {
            response.setResponses(createErrorResponse(FAILED_STATUS, "No valid owners found. At least one valid owner required."));
        }

        if (!warningMessages.isEmpty()) {
            GenericMessage responseMessage = response.getResponses() != null ? 
                response.getResponses() : new GenericMessage();
            responseMessage.setWarnings(warningMessages);
            response.setResponses(responseMessage);
        }

        return ownerReferences;
    }

    private void processCatalogMetadata(PublishCatalogRequestVO request, 
            FabricWorkspaceVO existingFabricWorkspace, 
            List<EntityReference> ownerReferences,
            FabricCatalogMetadataDetailsVO catalogMetadataDetails) {
        
        DatabaseService databaseService = openMetadataClient.createDatabaseService(
                existingFabricWorkspace.getName(),
                ownerReferences,
                existingFabricWorkspace.getDescription(),
                request.getMandatoryFields().getTier());

        for (DatabaseMetadataVO dbMetadata : request.getMetadata().getDatabases()) {
            Database database = openMetadataClient.createDatabase(
                    dbMetadata.getDbName(),
                    existingFabricWorkspace.getName(), 
                    request.getMandatoryFields(),
                    ownerReferences,
                    dbMetadata.getDescription());
                    
            for (SchemaMetadataVO schemaMetadata : dbMetadata.getSchemas()) {
                String schemaFqn = OpenMetadataFqnBuilder.build(
                    existingFabricWorkspace.getName(),
                    database.getName()
                );
                DatabaseSchema schema = openMetadataClient.createSchema(
                        schemaMetadata.getSchemaName(),
                        schemaFqn);

                for (TableMetadataVO tableMetadata : schemaMetadata.getTables()) {
                    String tableFqn = OpenMetadataFqnBuilder.build(
                        existingFabricWorkspace.getName(),
                        database.getName(),
                        schema.getName()
                    );
                    List<Column> columns = tableMetadata.getColumns().stream()
                        .map(col -> openMetadataClient.buildColumn(
                            col.getColumnName(),
                            null,
                            col.getColType(),
                            col.getColConstraint()
                        ))
                        .collect(Collectors.toList()); 

                    openMetadataClient.createTable(
                            tableMetadata.getTableName(),
                            tableFqn,
                            columns);
                }
            }
        }
    }

    private void saveCatalogMetadata(PublishCatalogRequestVO request, 
            FabricCatalogMetadataDetailsVO catalogMetadataDetails) {
        catalogMetadataDetails.setMetadata(request.getMetadata());
        catalogMetadataDetails.setOwners(request.getOwners());
        catalogMetadataDetails.setMandatoryFields(request.getMandatoryFields());
        catalogRepo.save(catalogAssembler.toEntity(catalogMetadataDetails));
    }

    private void prepareSuccessResponse(PublishCatalogResponseVO response, 
            FabricCatalogMetadataDetailsVO catalogMetadataDetails) {
        GenericMessage responseMessage = new GenericMessage();
        responseMessage.setSuccess(SUCCESS_STATUS);
        response.setResponses(responseMessage);
        response.setData(catalogMetadataDetails);
    }

    private FabricCatalogMetadataVO retrieveMetadataFromOpenMetadata(String serviceName) {
        FabricCatalogMetadataVO metadata = new FabricCatalogMetadataVO();
        metadata.setServiceName(serviceName);

        // 1. Get the database service
        DatabaseService service = openMetadataClient.getDatabaseService(serviceName);
        metadata.setServiceId(service.getId().toString());

        // 2. Get all databases for this service
        List<Database> databases = openMetadataClient.getDatabasesForService(serviceName);
        List<DatabaseMetadataVO> dbVos = new ArrayList<>();

        for (Database db : databases) {
            DatabaseMetadataVO dbVo = new DatabaseMetadataVO();
            dbVo.setDbName(db.getName());
            dbVo.setDbId(db.getId().toString());

            // 3. Get all schemas for this database
            List<DatabaseSchema> schemas = openMetadataClient.getSchemasForDatabase(db.getFullyQualifiedName());
            List<SchemaMetadataVO> schemaVos = new ArrayList<>();

            for (DatabaseSchema schema : schemas) {
                SchemaMetadataVO schemaVo = new SchemaMetadataVO();
                schemaVo.setSchemaName(schema.getName());
                schemaVo.setSchemaId(schema.getId().toString());

                // 4. Get all tables for this schema
                List<Table> tables = openMetadataClient.getTablesForSchema(schema.getFullyQualifiedName());
                List<TableMetadataVO> tableVos = new ArrayList<>();

                for (Table table : tables) {
                    TableMetadataVO tableVo = new TableMetadataVO();
                    tableVo.setTableName(table.getName());
                    tableVo.setTableId(table.getId().toString());

                    // 5. Get all columns for this table
                    List<ColumnMetadataVO> columnVos = new ArrayList<>();
                    if (table.getColumns() != null) {
                        for (Column column : table.getColumns()) {
                            ColumnMetadataVO colVo = new ColumnMetadataVO();
                            colVo.setColumnName(column.getName());
                            colVo.setColType(column.getDataType() != null ? 
                                column.getDataType().toString() : null);
                            colVo.setColConstraint(column.getConstraint() != null ? 
                                column.getConstraint().toString() : null);
                            columnVos.add(colVo);
                        }
                    }
                    tableVo.setColumns(columnVos);
                    tableVos.add(tableVo);
                }

                schemaVo.setTables(tableVos);
                schemaVos.add(schemaVo);
            }

            dbVo.setSchemas(schemaVos);
            dbVos.add(dbVo);
        }

        metadata.setDatabases(dbVos);
        return metadata;
    }

    private FabricCatalogMetadataDetailsVO retrieveStoredMetadataDetails(String serviceName, 
            FabricCatalogMetadataVO metadata) {
        FabricCatalogMetadataDetailsVO catalogMetadataDetails = new FabricCatalogMetadataDetailsVO();
        catalogMetadataDetails.setMetadata(metadata);

        FabricCatalogMetadataNsql entity = catalogCustomRepo.findByServiceName(serviceName)
            .orElseThrow(() -> new EntityNotFoundException("Catalog metadata", serviceName));
        FabricCatalogMetadataDetailsVO vo = catalogAssembler.toVo(entity);

        catalogMetadataDetails.setOwners(vo.getOwners());
        catalogMetadataDetails.setMandatoryFields(vo.getMandatoryFields());
        return catalogMetadataDetails;
    }

	private void handleDeletions(FabricCatalogMetadataVO existingMetadata, FabricCatalogMetadataVO newMetadata) {
		// 1. Find and delete removed databases (present in existing but not in new)
		List<DatabaseMetadataVO> deletedDbs = existingMetadata.getDatabases().stream()
			.filter(existingDb -> existingDb.getDbId() != null)
			.filter(existingDb -> newMetadata.getDatabases().stream()
				.noneMatch(newDb -> newDb.getDbId() != null && newDb.getDbId().equals(existingDb.getDbId())))
			.collect(Collectors.toList());
		
		// Delete the databases that were removed
		for (DatabaseMetadataVO db : deletedDbs) {
			try {
				log.info("Deleting database: {}", db.getDbName());
				openMetadataClient.deleteDatabase(db.getDbId());
			} catch (Exception e) {
				log.error("Failed to delete database {}: {}", db.getDbName(), e.getMessage());
			}
		}

		// 2. For databases that exist in both, check for deleted schemas
		for (DatabaseMetadataVO existingDb : existingMetadata.getDatabases()) {
			if (existingDb.getDbId() == null || deletedDbs.contains(existingDb)) {
				continue; // Skip if database was deleted or has no ID
			}

			Optional<DatabaseMetadataVO> matchingNewDb = newMetadata.getDatabases().stream()
				.filter(newDb -> newDb.getDbId() != null && newDb.getDbId().equals(existingDb.getDbId()))
				.findFirst();
				
			if (matchingNewDb.isPresent()) {
				processSchemaDeletions(existingDb, matchingNewDb.get());
			}
		}
	}

	private void processSchemaDeletions(DatabaseMetadataVO existingDb, DatabaseMetadataVO newDb) {
		List<SchemaMetadataVO> deletedSchemas = existingDb.getSchemas().stream()
			.filter(existingSchema -> existingSchema.getSchemaId() != null)
			.filter(existingSchema -> newDb.getSchemas().stream()
				.noneMatch(newSchema -> newSchema.getSchemaId() != null && 
						newSchema.getSchemaId().equals(existingSchema.getSchemaId())))
			.collect(Collectors.toList());
		
		// Delete the schemas that were removed
		for (SchemaMetadataVO schema : deletedSchemas) {
			try {
				log.info("Deleting schema: {}", schema.getSchemaName());
				openMetadataClient.deleteSchema(schema.getSchemaId());
			} catch (Exception e) {
				log.error("Failed to delete schema {}: {}", schema.getSchemaName(), e.getMessage());
			}
		}

		// 3. For schemas that exist in both, check for deleted tables
		for (SchemaMetadataVO existingSchema : existingDb.getSchemas()) {
			if (existingSchema.getSchemaId() == null || deletedSchemas.contains(existingSchema)) {
				continue; // Skip if schema was deleted or has no ID
			}

			Optional<SchemaMetadataVO> matchingNewSchema = newDb.getSchemas().stream()
				.filter(newSchema -> newSchema.getSchemaId() != null && 
						newSchema.getSchemaId().equals(existingSchema.getSchemaId()))
				.findFirst();
				
			if (matchingNewSchema.isPresent()) {
				processTableDeletions(existingSchema, matchingNewSchema.get());
			}
		}
	}

	private void processTableDeletions(SchemaMetadataVO existingSchema, SchemaMetadataVO newSchema) {
		List<TableMetadataVO> deletedTables = existingSchema.getTables().stream()
			.filter(existingTable -> existingTable.getTableId() != null)
			.filter(existingTable -> newSchema.getTables().stream()
				.noneMatch(newTable -> newTable.getTableId() != null && 
						newTable.getTableId().equals(existingTable.getTableId())))
			.collect(Collectors.toList());
		
		// Delete the tables that were removed
		for (TableMetadataVO table : deletedTables) {
			try {
				log.info("Deleting table: {}", table.getTableName());
				openMetadataClient.deleteTable(table.getTableId());
			} catch (Exception e) {
				log.error("Failed to delete table {}: {}", table.getTableName(), e.getMessage());
			}
		}

		// 4. For tables that exist in both, check for deleted columns
		for (TableMetadataVO existingTable : existingSchema.getTables()) {
			if (existingTable.getTableId() == null || deletedTables.contains(existingTable)) {
				continue; // Skip if table was deleted or has no ID
			}

			Optional<TableMetadataVO> matchingNewTable = newSchema.getTables().stream()
				.filter(newTable -> newTable.getTableId() != null && 
						newTable.getTableId().equals(existingTable.getTableId()))
				.findFirst();
				
			if (matchingNewTable.isPresent()) {
				processColumnDeletions(existingTable, matchingNewTable.get());
			}
		}
	}

	private void processColumnDeletions(TableMetadataVO existingTable, TableMetadataVO newTable) {
		List<ColumnMetadataVO> deletedColumns = existingTable.getColumns().stream()
			.filter(existingCol -> existingCol.getColumnName() != null)
			.filter(existingCol -> newTable.getColumns().stream()
				.noneMatch(newCol -> newCol.getColumnName() != null && 
						newCol.getColumnName().equals(existingCol.getColumnName())))
			.collect(Collectors.toList());
		
		// Delete the columns that were removed
		for (ColumnMetadataVO column : deletedColumns) {
			try {
				log.info("Deleting column: {} from table {}", column.getColumnName(), existingTable.getTableName());
				openMetadataClient.deleteColumnFromTable(
					existingTable.getTableId(), 
					column.getColumnName());
			} catch (Exception e) {
				log.error("Failed to delete column {}: {}", column.getColumnName(), e.getMessage());
			}
		}
	}

    private void processUpdates(PublishCatalogRequestVO request, String serviceName, List<EntityReference> ownerReferences) {
        // Update or create all databases
        request.getMetadata().getDatabases().forEach(dbMetadata -> 
            updateDatabase(dbMetadata, serviceName, request.getMandatoryFields(), ownerReferences));
    }
    
    private void updateStoredMetadata(PublishCatalogRequestVO request) {
        try {
            FabricCatalogMetadataNsql entity = catalogCustomRepo.findByServiceName(request.getMetadata().getServiceName())
                .orElseThrow(() -> new EntityNotFoundException("Catalog metadata", request.getMetadata().getServiceName()));
            FabricCatalogMetadataDetailsVO vo = catalogAssembler.toVo(entity);
            
            // Update all fields
            vo.setMetadata(request.getMetadata());
            vo.setOwners(request.getOwners());
            vo.setMandatoryFields(request.getMandatoryFields());
            
            catalogRepo.save(catalogAssembler.toEntity(vo));
        } catch (EntityNotFoundException e) {
            log.error("Catalog metadata not found for service: {}", request.getMetadata().getServiceName());
            throw new EntityNotFoundException("Catalog metadata", request.getMetadata().getServiceName());
        } catch (Exception e) {
            log.error("Failed to update catalog metadata: {}", e.getMessage());
            throw new OpenMetadataClientException("Failed to update catalog metadata: " + e.getMessage(), e);
        }
    }

    private void updateLakeHouseDetails(FabricWorkspaceVO workspace, FabricCatalogMetadataVO metadata) {
        try {
            CdcPublishedLakeHouseDetailsVO details = Optional.ofNullable(workspace.getCdcPublishedLakeHouseDetails())
                .orElse(new CdcPublishedLakeHouseDetailsVO());
                
            details.setIsLakeHousesPublishedToCdc(true);
            details.setPublishedLakeHouseNames(
                metadata.getDatabases().stream()
                    .map(DatabaseMetadataVO::getDbId)
                    .collect(Collectors.toList()));
            
            workspace.setCdcPublishedLakeHouseDetails(details);
            jpaRepo.save(assembler.toEntity(workspace));
        } catch (Exception e) {
            log.error("Failed to update lake house details: {}", e.getMessage());
            throw new OpenMetadataClientException("Failed to update lake house details: " + e.getMessage(), e);
        }
    }

    private void updateServiceOwners(DatabaseService service, List<EntityReference> newOwners) {
        CreateDatabaseService updateRequest = new CreateDatabaseService()
            .name(service.getName())
            .serviceType(CreateDatabaseService.ServiceTypeEnum.DATALAKE)
            .connection(service.getConnection())
            .tags(service.getTags())
            .description(service.getDescription())
            .owners(newOwners);
        
        openMetadataClient.updateDatabaseService(updateRequest);
    }

    private void updateDatabase(DatabaseMetadataVO dbMetadata, String serviceName, 
        MandatoryFieldsVO fields, List<EntityReference> ownerReferences) {
		try {
			Database database;
			
			if (dbMetadata.getDbId() != null) {
				// Existing database - update it
				database = openMetadataClient.updateDatabase(
					dbMetadata.getDbId(),
					dbMetadata.getDbName(),
					serviceName,
					fields,
					ownerReferences);
			} else {
				// New database - create it
				database = openMetadataClient.createDatabase(
					dbMetadata.getDbName(),
					serviceName,
					fields,
					ownerReferences,
                    dbMetadata.getDescription());
				
				// Set the new ID back to the metadata
				dbMetadata.setDbId(database.getId().toString());
				
				// Get the full database entity with service reference
				database = openMetadataClient.getDatabaseById(database.getId().toString());
			}

			for (SchemaMetadataVO schemaMetadata : dbMetadata.getSchemas()) {
				updateSchema(schemaMetadata, database);
			}
		} catch (Exception e) {
			throw new OpenMetadataClientException("Failed to process database: " + dbMetadata.getDbName(), e);
		}
	}

    private void updateSchema(SchemaMetadataVO schemaMetadata, Database database) {
		try {
			// Build schema FQN using the actual database's service FQN
			String schemaFqn = OpenMetadataFqnBuilder.build(
				database.getService().getFullyQualifiedName(),
				database.getName());
			
			DatabaseSchema schema;
			
			if (schemaMetadata.getSchemaId() != null) {
				// Existing schema - update it
				schema = openMetadataClient.updateSchema(
					schemaMetadata.getSchemaId(),
					schemaMetadata.getSchemaName(),
					schemaFqn);
			} else {
				// New schema - create it
				schema = openMetadataClient.createSchema(
					schemaMetadata.getSchemaName(),
					schemaFqn);
				
				// Set the new ID back to the metadata
				schemaMetadata.setSchemaId(schema.getId().toString());
				
				// Get the full schema entity
				schema = openMetadataClient.getSchemaById(schema.getId().toString());
			}

			for (TableMetadataVO tableMetadata : schemaMetadata.getTables()) {
				updateTable(tableMetadata, schema, database);
			}
		} catch (Exception e) {
			throw new OpenMetadataClientException("Failed to update schema: " + schemaMetadata.getSchemaName(), e);
		}
	}

    private void updateTable(TableMetadataVO tableMetadata, DatabaseSchema schema, Database database) {
        try {
            String tableFqn = OpenMetadataFqnBuilder.build(
                database.getService().getFullyQualifiedName(),
                database.getName(),
                schema.getName());
            
            List<Column> columns = tableMetadata.getColumns().stream()
                .map(col -> openMetadataClient.buildColumn(
                    col.getColumnName(),
                    null,
                    col.getColType(),
                    col.getColConstraint()))
                .collect(Collectors.toList());

            if (tableMetadata.getTableId() != null) {
                // Existing table - update it
                openMetadataClient.updateTable(
                    tableMetadata.getTableId(),
                    tableMetadata.getTableName(),
                    tableFqn,
                    columns);
            } else {
                // New table - create it
                Table table = openMetadataClient.createTable(
                    tableMetadata.getTableName(),
                    tableFqn,
                    columns);
                
                // Set the new ID back to the metadata
                tableMetadata.setTableId(table.getId().toString());
            }
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to update table: " + tableMetadata.getTableName(), e);
        }
    }

    private GenericMessage createErrorResponse(String status, String message) {
        GenericMessage response = new GenericMessage();
        MessageDescription msg = new MessageDescription();
        msg.setMessage(message);
        response.setErrors(Collections.singletonList(msg));
        response.setSuccess(status);
        return response;
    }

    public LakehouseObjectsResponseVO getLakehouseObjects(String workspaceId, String lakehouseName, String schemaName) {
        log.info("Fetching lakehouse object details for: {}", lakehouseName);
        try {
            return cdcPushServiceClient.getLakehouseObjects(workspaceId, lakehouseName, schemaName);
        } catch (Exception e) {
            log.error("Failed to fetch lakehouse object details for {}: {}", lakehouseName, e.getMessage());
          //  throw new OpenMetadataClientException("Failed to fetch lakehouse object details for " + lakehouseName + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void addWorkspaceGropusToLakehouse(String workspaceId, String lakehouseId, String workspaceName, String lakehouseName, List<String> groupNames, String ddxId) {

        log.info("Adding workspace groups to lakehouse: {}, groups: {}, workspace: {}", lakehouseName, groupNames, workspaceName);

        DdxDataProductsDetailsNsql dbLakehouseDetails = ddxDataProductsDetailsRepository.findById(lakehouseId).orElse(null);
        
        if(dbLakehouseDetails == null || dbLakehouseDetails.getData() == null || dbLakehouseDetails.getData().getDdxProducts() == null){
            log.error("Lakehouse details not found for lakehouseId: {}", lakehouseId);
            throw new EntityNotFoundException("Lakehouse details", lakehouseId);
        }
        DdxProduct ddxProduct = dbLakehouseDetails.getData().getDdxProducts().stream().filter(product -> product.getProductId().equals(ddxId)).findFirst().orElse(null);
        if(ddxProduct == null){
            log.error("DdxProduct not found for ddxId: {}", ddxId);
            throw new EntityNotFoundException("DdxProduct", ddxId);
        }

        List<String> validGroups = checkForValidGroups(groupNames);
        if(validGroups.isEmpty()){
            log.warn("No valid groups found to add to lakehouse: {}", lakehouseName);
            throw new RuntimeException("No valid groups found to add to lakehouse: " + lakehouseName);
        }

        Fabric2FabricDetail fabric2FabricDetail = new Fabric2FabricDetail(); 
        fabric2FabricDetail.setInitiatedOn(new Date());
        fabric2FabricDetail.setIsFabric2Fabric(Boolean.TRUE);
        fabric2FabricDetail.setGroupsNames(new ArrayList<>());

        GroupNameDetail groupNameDetail = new GroupNameDetail();
        groupNameDetail.setGroupNameList(buildGroupStatusList(validGroups, ConstantsUtility.GROUPS_IN_PROGRESS_CONSTANT));
        
        // Adding the TRS user in the workspace to make sure the user has access to the lakehouse where the groups are being added. 
        // This is required as part of the UIlicious test case which adds the groups to the lakehouse and needs access to the lakehouse.
        fabricWorkspaceClient.addUser(workspaceId, pidUser);

        String testRunID = null;
        try{
            testRunID = this.uiLiciousClient.addWorkspaceGroupsToLakehouse(workspaceId, lakehouseId, workspaceName, lakehouseName, validGroups);
            groupNameDetail.setRunStatus(ConstantsUtility.GROUPS_IN_PROGRESS_CONSTANT); 
            groupNameDetail.setTestRunId(testRunID);
            fabric2FabricDetail.getGroupsNames().add(groupNameDetail);
            if(ddxProduct.getFabric2fabricDetails() == null){
                ddxProduct.setFabric2fabricDetails(new ArrayList<>());
            }
            ddxProduct.getFabric2fabricDetails().add(fabric2FabricDetail);
        }catch(Exception e){
            log.error("Failed to add workspace groups to lakehouse {}: {}", lakehouseName, e.getMessage());
            throw new RuntimeException("Failed to add workspace groups to lakehouse " + lakehouseName + ": " + e.getMessage(), e);
        }

        try {
            log.info("Saving workspace group details to DB for workspace: {}", workspaceName);
            ddxDataProductsDetailsRepository.save(dbLakehouseDetails);
        } catch (Exception e) {
            log.error("Failed to save workspace group details to DB for workspace {}: {}", workspaceName, e.getMessage());
            throw new RuntimeException("Failed to save workspace group details to DB for workspace " + workspaceName + ": " + e.getMessage(), e);
        }

    }

    @Override
    public List<GroupStatusResponseVO> getGroupsAssignmentStatus(String workspaceName, String workspaceId, String lakehouseName, String lakehouseId,
            List<String> groupName, String ddxId) {
        log.info("Getting groups assigning status for lakehouse: {}, groups: {}, workspace: {}", lakehouseId, groupName, workspaceId);
        try{

            DdxDataProductsDetailsNsql dbEntity = ddxDataProductsDetailsRepository.findById(lakehouseId).orElse(null);
            if(dbEntity == null || dbEntity.getData() == null || dbEntity.getData().getDdxProducts() == null){
                log.error("Fabric workspace not found for name: {}", workspaceName);
                throw new EntityNotFoundException("Fabric workspace", workspaceName);
            }
            DdxProduct ddxProduct = dbEntity.getData().getDdxProducts().stream().filter(product -> product.getProductId().equals(ddxId)).findFirst().orElse(null);

            // DdxPublishedLakeHouseDetailsVO ddxPublishedLakeHouseDetails = ddxDataProductsDetailsAssembler.toVo(dbEntity);
            List<String> validGroups = checkForValidGroups(groupName);
            if(validGroups.isEmpty()){
                log.warn("No valid groups found for lakehouse: {}, workspace: {}", lakehouseName, workspaceName);
                throw new IllegalArgumentException("No valid groups found for lakehouse: " + lakehouseName + ", workspace: " + workspaceName);
            }

            if(ddxProduct == null || ddxProduct.getFabric2fabricDetails() == null || ddxProduct.getFabric2fabricDetails().isEmpty()){
                log.warn("No group details found for workspace: {}, and the test run ID is not available", workspaceId);
                throw new EntityNotFoundException("Group details", workspaceId);
            }

            List<GroupStatusResponseVO> finalGroupStatusList = null;
            boolean allRecordsCompleted = true;
            for(Fabric2FabricDetail fabric2FabricDetails : ddxProduct.getFabric2fabricDetails()){
                for(GroupNameDetail groupNameDetail : fabric2FabricDetails.getGroupsNames()){
                    List<GroupStatusResponseVO> groupStatusList = null;
                    List<String> groupNameList = groupNameDetail.getGroupNameList().stream().map(group->group.getGroupName()).collect(Collectors.toList());
                    Boolean sameGroup = new HashSet<>(groupNameList).equals(new HashSet<>(validGroups));
                    if(groupNameDetail.getRunStatus().equals(ConstantsUtility.GROUPS_COMPLETED_CONSTANT) && !sameGroup){
                        continue;
                    }
                    if(sameGroup){
                        if(groupNameDetail.getRunStatus().equals(ConstantsUtility.GROUPS_COMPLETED_CONSTANT)){
                            log.info("Groups: {} have been successfully added to lakehouse: {}", validGroups, lakehouseName);
                            groupStatusList = groupNameDetail.getGroupNameList().stream().map(groupStatus -> {
                                GroupStatusResponseVO response = new GroupStatusResponseVO();
                                response.setGroupName(groupStatus.getGroupName());
                                response.setStatus(groupStatus.getStatus());
                                response.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(groupStatus.getStatus()));
                                return response;
                            }).collect(Collectors.toList());
                        } else {
                            log.info("Groups: {} are still being added to lakehouse: {}, current status: {}", validGroups, lakehouseName, groupNameDetail.getRunStatus());
                            finalGroupStatusList = this.uiLiciousClient.getStatusOfGroupsAdditionToLakehouse(workspaceName, workspaceId, lakehouseName, lakehouseId, validGroups, groupNameDetail.getTestRunId());
                            groupStatusList = finalGroupStatusList;
                        }
                    }
                    Boolean unProcessGroupRun = groupNameDetail.getGroupNameList().stream().anyMatch(group-> group.getStatus().equals("IN_PROGRESS"));
                    if(unProcessGroupRun){
                        allRecordsCompleted = false;
                        groupStatusList = this.uiLiciousClient.getStatusOfGroupsAdditionToLakehouse(workspaceName, workspaceId, lakehouseName, lakehouseId, validGroups, groupNameDetail.getTestRunId());
                    }

                    if(groupStatusList != null && !groupStatusList.isEmpty()){
                        log.info("Group status for lakehouseId {}: {}", lakehouseId, groupStatusList);
                        Map<String, String> statusMap = groupStatusList.stream().collect(Collectors.toMap(GroupStatusResponseVO::getGroupName, GroupStatusResponseVO::getStatus));
                        groupNameDetail.getGroupNameList().forEach(group->{
                            group.setStatus(statusMap.get(group.getGroupName()));
                            group.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(group.getStatus()));
                        });

                        groupNameDetail.setRunStatus(groupStatusList.stream().allMatch(status -> ConstantsUtility.GROUPS_ADDED_CONSTANT.equals(status.getStatus())) ? ConstantsUtility.GROUPS_COMPLETED_CONSTANT : ConstantsUtility.GROUPS_IN_PROGRESS_CONSTANT);

                    }
                    if(groupStatusList != null && groupStatusList.isEmpty()){
                        allRecordsCompleted = false;
                    }
                }
            }
            
            // DdxDataProductsDetailsNsql saveDBEntity = ddxDataProductsDetailsAssembler.toEntity(ddxPublishedLakeHouseDetails);
            ddxDataProductsDetailsRepository.save(dbEntity);

            // List<String> testRunIDList;
            // WorkspaceGroupsDetailsVO dbWorkspaceVO = workspaceVO.getWorkspaceGroupsDetails().stream()
            //         .filter(groupDetails -> groupDetails.getLakehouseName().equals(lakehouseName))
            //         .findFirst().orElse(null); // fetching the test run ID from the DB using lakehouseId and groupName
            // if(dbWorkspaceVO == null){
            //     log.warn("No group details found for lakehouse: {}, workspace: {}, and the test run ID is not available", lakehouseName, workspaceName);
            //     return Collections.emptyList();
            // } else {
            //     testRunIDList = dbWorkspaceVO.getTestRunId();
            //     if(testRunIDList == null || testRunIDList.isEmpty()){
            //         log.warn("The test run ID list is empty for lakehouse: {}, workspace: {}", lakehouseName, workspaceName);
            //         return Collections.emptyList();
            //     }
            // }
            // List<GroupStatusResponseVO> groupStatusList = this.uiLiciousClient.getStatusOfGroupsAdditionToLakehouse(workspaceName, workspaceId, lakehouseName, lakehouseId, 
            //     checkForValidGroups(groupName), groupNameDetail.getTestRunId());
            // if(groupStatusList.isEmpty()){
            //     log.warn("No group status found for lakehouseId: {}", lakehouseId);
            // } else {
            //     log.info("Group status for lakehouseId {}: {}", lakehouseId, groupStatusList);
            //     System.err.println("DB Workspace VO: " + dbWorkspaceVO.getGroups().toString());
            //     dbWorkspaceVO.getGroups().forEach(groupDetails -> {
            //         groupStatusList.forEach(status -> {
            //             if(groupDetails.getGroupName().equals(status.getGroupName())){
            //                 groupDetails.setStatus(GroupAssignmentStatusVO.StatusEnum.fromValue(status.getStatus().toString()));
            //             }
            //         });
            //     });
            // }


            if(allRecordsCompleted){
                this.fabricWorkspaceClient.removeUserGroup(workspaceId, pidUserIdentifier);
            }
            // DB operations to update the status of the workspace groups addition to lakehouse can be done here using the groupStatusList
            // jpaRepo.save(assembler.toEntity(workspaceVO));

            return finalGroupStatusList;

        }catch(Exception e){
            log.error("Failed to get groups assigning status for lakehouse {}: {}", lakehouseId, e.getMessage());
            throw new RuntimeException("Failed to get groups assigning status for lakehouse " + lakehouseId + ": " + e.getMessage(), e);
        }
    }

    private void triggerUiLiciousForLakehouses(FabricWorkspaceVO workspace) {
        String workspaceId = workspace.getId();
        String workspaceName = workspace.getName();
        List<FabricLakehouseVO> lakehouses = workspace.getLakehouses();
        if (lakehouses == null || lakehouses.isEmpty()) {
            log.warn("No lakehouses found for workspace: {}, skipping UiLicious trigger", workspaceName);
            return;
        }
        for (FabricLakehouseVO lakehouse : lakehouses) {
            try {
                String testRunId = uiLiciousClient.addServicePrincipalToLakehouse(
                        workspaceId, lakehouse.getId(), workspaceName, lakehouse.getName(), null);
                if (testRunId != null) {
                    log.info("UiLicious test run triggered with testRunId: {} for workspace: {} and lakehouse: {}",
                            testRunId, workspaceName, lakehouse.getName());
                } else {
                    log.warn("UiLicious test run returned null testRunId for workspace: {} and lakehouse: {}",
                            workspaceName, lakehouse.getName());
                }
            } catch (Exception e) {
                log.error("Failed to trigger UiLicious for workspace: {} and lakehouse: {}",
                        workspaceName, lakehouse.getName(), e);
            }
        }
    }

    private List<String> checkForValidGroups(List<String> groups){

        List<String> validGroups = new ArrayList<>();
        for(String groupId : groups){
            if (groupId == null || groupId.trim().isEmpty()) {
                log.warn("Skipping empty or null group ID");
                continue;
            }
            String trimmedGroupId = groupId.trim();
            // Validate group exists in the system before making API call to UiLicious
            try {
                if (trimmedGroupId == null /*|| !fabricWorkspaceClient.checkGroupExists(trimmedGroupId)*/) {
                    log.error("Group {} not found", trimmedGroupId);
                    continue;
                }
            } catch (Exception e) {
                log.error("Error checking if group {} exists: {}", trimmedGroupId, e.getMessage());
                continue;
            }
            validGroups.add(trimmedGroupId);
        }
        return validGroups;
    }

    public static List<GroupStatusResponseVO> buildGroupStatusListResponse(List<String> groups, String status){
        List<GroupStatusResponseVO> groupStatusList = new ArrayList<>();
        for(String groupName : groups){
            GroupStatusResponseVO statusVO = new GroupStatusResponseVO();
            statusVO.setGroupName(groupName);
            statusVO.setStatus(status);
            statusVO.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(status));
            groupStatusList.add(statusVO);
        }
        return groupStatusList;
    }

    private List<GroupNameList> buildGroupStatusList(List<String> groups, String status){
        List<GroupNameList> groupStatusList = new ArrayList<>();
        for(String groupName : groups){
            GroupNameList statusVO = new GroupNameList();
            statusVO.setGroupName(groupName);
            statusVO.setStatus(status);
            statusVO.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(status));
            groupStatusList.add(statusVO);
        }
        return groupStatusList;
    }

    @Override
    @Transactional
    public MirroredCatalogResponseVO createMirroredCatalog(CreateMirroredCatalogRequestVO request, String ddxId, String lakehouseId) {
        log.info("Creating mirrored catalog for ddxId: {}, lakehouseId: {}, catalogName: {}", ddxId, lakehouseId, request.getCatalogName());

        String catalogName = request.getCatalogName();
        String ddxGroup = request.getDdxGroup();

        DdxMirroredCatalogProductNsql existingEntity = mirroredCatalogCustomRepo.findByCatalogName(catalogName);

        if (existingEntity != null) {
            DdxMirroredCatalogProduct existingData = existingEntity.getData();
            List<DdxGroupDetail> existingGroups = existingData.getDdxGroupDetails();
            if (existingGroups != null) {
                boolean groupExists = existingGroups.stream()
                        .anyMatch(g -> ddxGroup.equals(g.getGroupName()));
                if (groupExists) {
                    log.info("Group {} already exists for catalog {}", ddxGroup, catalogName);
                    MirroredCatalogResponseVO response = new MirroredCatalogResponseVO();
                    response.setDdxCorrelationId(existingData.getDdxCorrelationId());
                    response.setStatus(ConstantsUtility.MIRRORED_CATALOG_ALREADY_EXISTS);
                    response.setMessage("Group " + ddxGroup + " already exists for catalog " + catalogName);
                    response.setDdxGroupDetails(mapDdxGroupDetailsToVO(existingGroups));
                    return response;
                }
            }

            Map<String, String> uiliciousResponse = callUiliciousDummy(catalogName, ddxGroup, false);

            DdxGroupDetail newGroupDetail = new DdxGroupDetail();
            newGroupDetail.setGroupName(ddxGroup);
            newGroupDetail.setGroupAddedStatus(uiliciousResponse.get("groupAddedStatus"));
            newGroupDetail.setGrantPermissionStatus(uiliciousResponse.get("grantPermissionStatus"));
            newGroupDetail.setTestRunId(uiliciousResponse.get("testRunId"));
            newGroupDetail.setMessage(uiliciousResponse.get("message"));

            if (existingGroups == null) {
                existingData.setDdxGroupDetails(new ArrayList<>());
            }
            existingData.getDdxGroupDetails().add(newGroupDetail);
            mirroredCatalogRepo.save(existingEntity);

            log.info("Added group {} to existing catalog {} with status {}", ddxGroup, catalogName, uiliciousResponse.get("groupAddedStatus"));

            MirroredCatalogResponseVO response = new MirroredCatalogResponseVO();
            response.setDdxCorrelationId(existingData.getDdxCorrelationId());
            response.setStatus(uiliciousResponse.get("groupAddedStatus"));
            response.setMessage(uiliciousResponse.get("message"));
            response.setDdxGroupDetails(mapDdxGroupDetailsToVO(existingData.getDdxGroupDetails()));
            return response;
        }

        Map<String, String> uiliciousResponse = callUiliciousDummy(catalogName, ddxGroup, true);

        MirroredCatalogDetail mirrorCatalogDetails = new MirroredCatalogDetail();
        mirrorCatalogDetails.setMirroredCatalogId(uiliciousResponse.get("catalogId"));
        mirrorCatalogDetails.setMirrorCatalogName(uiliciousResponse.get("mirrorCatalogName"));
        mirrorCatalogDetails.setCatalogStatus(uiliciousResponse.get("catalogStatus"));
        mirrorCatalogDetails.setMessage(uiliciousResponse.get("message"));

        DdxGroupDetail firstGroupDetail = new DdxGroupDetail();
        firstGroupDetail.setGroupName(ddxGroup);
        firstGroupDetail.setGroupAddedStatus(uiliciousResponse.get("groupAddedStatus"));
        firstGroupDetail.setGrantPermissionStatus(uiliciousResponse.get("grantPermissionStatus"));
        firstGroupDetail.setTestRunId(uiliciousResponse.get("testRunId"));
        firstGroupDetail.setMessage(uiliciousResponse.get("message"));

        List<MirroredObjectDetail> objects = null;
        if (request.getObjects() != null) {
            objects = request.getObjects().stream().map(obj -> {
                MirroredObjectDetail detail = new MirroredObjectDetail();
                detail.setObjectName(obj.getObjectName());
                detail.setObjectType(obj.getObjectType() != null ? obj.getObjectType().toString() : null);
                return detail;
            }).collect(Collectors.toList());
        }

        DdxMirroredCatalogProduct data = new DdxMirroredCatalogProduct();
        data.setDdxId(ddxId);
        data.setLakehouseId(lakehouseId);
        data.setCatalogName(catalogName);
        data.setSchemaName(request.getSchemaName());
        data.setRegion(request.getRegion());
        data.setFullSchema(request.isFullSchema());
        data.setObjects(objects);
        data.setStorageAccountUrl(request.getStorageAccountUrl());
        data.setDdxCorrelationId(request.getDdxCorrelationId());
        data.setDdxGroupPermission(request.getDdxGroupPermission());
        data.setWorkspaceId(centralWorkspaceId);
        data.setWorkspaceName(centralWorkspaceName);
        data.setStatus(ConstantsUtility.MIRRORED_CATALOG_IN_PROGRESS);
        data.setInitiatedOn(new Date());
        data.setMirrorCatalogDetails(mirrorCatalogDetails);
        data.setDdxGroupDetails(new ArrayList<>(Collections.singletonList(firstGroupDetail)));

        DdxMirroredCatalogProductNsql entity = new DdxMirroredCatalogProductNsql();
        entity.setId(UUID.randomUUID().toString());
        entity.setData(data);
        mirroredCatalogRepo.save(entity);

        log.info("Created mirrored catalog {} for ddxId: {} with correlationId: {}", catalogName, ddxId, request.getDdxCorrelationId());

        MirroredCatalogResponseVO response = buildMirroredCatalogResponse(data);
        return response;
    }

    @Override
    public MirroredCatalogResponseVO getMirroredCatalogStatus(String ddxCorrelationId, String ddxId, String lakehouseId) {
        log.info("Getting mirrored catalog status for correlationId: {}, ddxId: {}, lakehouseId: {}", ddxCorrelationId, ddxId, lakehouseId);

        DdxMirroredCatalogProductNsql entity = mirroredCatalogCustomRepo.findByCorrelationId(ddxCorrelationId);
        if (entity == null || entity.getData() == null) {
            log.error("No mirrored catalog record found for correlationId: {}", ddxCorrelationId);
            return null;
        }

        DdxMirroredCatalogProduct data = entity.getData();
        if (!ddxId.equals(data.getDdxId()) || !lakehouseId.equals(data.getLakehouseId())) {
            log.error("Mirrored catalog record mismatch: expected ddxId={}, lakehouseId={} but found ddxId={}, lakehouseId={}",
                    ddxId, lakehouseId, data.getDdxId(), data.getLakehouseId());
            return null;
        }

        return buildMirroredCatalogResponse(data);
    }

    @Override
    @Transactional
    public MirroredCatalogResponseVO updateMirroredCatalogStatus(UpdateMirroredCatalogStatusRequestVO request, String ddxId, String lakehouseId) {
        log.info("Updating mirrored catalog status for correlationId: {}", request.getDdxCorrelationId());

        DdxMirroredCatalogProductNsql entity = mirroredCatalogCustomRepo.findByCorrelationId(request.getDdxCorrelationId());
        if (entity == null || entity.getData() == null) {
            log.error("No mirrored catalog record found for correlationId: {}", request.getDdxCorrelationId());
            return null;
        }

        DdxMirroredCatalogProduct data = entity.getData();

        if (request.getCatalogStatus() != null || request.getMirroredCatalogId() != null
                || request.getMirrorCatalogName() != null || request.getCatalogMessage() != null) {
            MirroredCatalogDetail catalogDetail = data.getMirrorCatalogDetails();
            if (catalogDetail == null) {
                catalogDetail = new MirroredCatalogDetail();
                data.setMirrorCatalogDetails(catalogDetail);
            }
            if (request.getMirroredCatalogId() != null) {
                catalogDetail.setMirroredCatalogId(request.getMirroredCatalogId());
            }
            if (request.getMirrorCatalogName() != null) {
                catalogDetail.setMirrorCatalogName(request.getMirrorCatalogName());
            }
            if (request.getCatalogStatus() != null) {
                catalogDetail.setCatalogStatus(request.getCatalogStatus());
            }
            if (request.getCatalogMessage() != null) {
                catalogDetail.setMessage(request.getCatalogMessage());
            }
        }

        if (request.getDdxGroupDetails() != null && data.getDdxGroupDetails() != null) {
            for (DdxGroupDetailVO groupUpdate : request.getDdxGroupDetails()) {
                for (DdxGroupDetail existingGroup : data.getDdxGroupDetails()) {
                    if (groupUpdate.getGroupName() != null && groupUpdate.getGroupName().equals(existingGroup.getGroupName())) {
                        if (groupUpdate.getGroupAddedStatus() != null) {
                            existingGroup.setGroupAddedStatus(groupUpdate.getGroupAddedStatus());
                        }
                        if (groupUpdate.getGrantPermissionStatus() != null) {
                            existingGroup.setGrantPermissionStatus(groupUpdate.getGrantPermissionStatus());
                        }
                        if (groupUpdate.getTestRunId() != null) {
                            existingGroup.setTestRunId(groupUpdate.getTestRunId());
                        }
                        if (groupUpdate.getMessage() != null) {
                            existingGroup.setMessage(groupUpdate.getMessage());
                        }
                        break;
                    }
                }
            }
        }

        String derivedStatus = deriveOverallStatus(data);
        data.setStatus(derivedStatus);

        if (ConstantsUtility.MIRRORED_CATALOG_SUCCESS.equals(derivedStatus)
                || ConstantsUtility.MIRRORED_CATALOG_FAILURE.equals(derivedStatus)) {
            data.setCompletedOn(new Date());
        }

        mirroredCatalogRepo.save(entity);
        log.info("Updated mirrored catalog status to {} for correlationId: {}", derivedStatus, request.getDdxCorrelationId());

        return buildMirroredCatalogResponse(data);
    }

    // TODO: Replace with actual Uilicious API integration
    private Map<String, String> callUiliciousDummy(String catalogName, String ddxGroup, boolean isNewCatalog) {
        log.info("Calling Uilicious dummy method for catalog: {}, group: {}, isNewCatalog: {}", catalogName, ddxGroup, isNewCatalog);
        Map<String, String> response = new HashMap<>();

        if (isNewCatalog) {
            response.put("mirrorCatalogName", catalogName + "_mirror");
            response.put("catalogId", UUID.randomUUID().toString());
            response.put("catalogStatus", ConstantsUtility.MIRRORED_CATALOG_SUCCESS);
            response.put("groupAdded", ddxGroup);
            response.put("groupAddedStatus", ConstantsUtility.MIRRORED_CATALOG_SUCCESS);
            response.put("grantPermissionStatus", ConstantsUtility.MIRRORED_CATALOG_SUCCESS);
            response.put("testRunId", "dummy-run-" + UUID.randomUUID().toString());
            response.put("message", "Dummy: Catalog created and group added successfully");
        } else {
            response.put("groupAdded", ddxGroup);
            response.put("groupAddedStatus", ConstantsUtility.MIRRORED_CATALOG_IN_PROGRESS);
            response.put("grantPermissionStatus", ConstantsUtility.MIRRORED_CATALOG_IN_PROGRESS);
            response.put("testRunId", "dummy-run-" + UUID.randomUUID().toString());
            response.put("message", "Dummy: Group addition in progress");
        }

        return response;
    }

    private String deriveOverallStatus(DdxMirroredCatalogProduct data) {
        MirroredCatalogDetail catalogDetail = data.getMirrorCatalogDetails();
        List<DdxGroupDetail> groups = data.getDdxGroupDetails();

        String catalogStatus = (catalogDetail != null) ? catalogDetail.getCatalogStatus() : null;

        if (ConstantsUtility.MIRRORED_CATALOG_FAILURE.equals(catalogStatus)) {
            return ConstantsUtility.MIRRORED_CATALOG_FAILURE;
        }
        if (groups != null) {
            for (DdxGroupDetail group : groups) {
                if (ConstantsUtility.MIRRORED_CATALOG_FAILURE.equals(group.getGroupAddedStatus())
                        || ConstantsUtility.MIRRORED_CATALOG_FAILURE.equals(group.getGrantPermissionStatus())) {
                    return ConstantsUtility.MIRRORED_CATALOG_FAILURE;
                }
            }
        }

        boolean allSuccess = ConstantsUtility.MIRRORED_CATALOG_SUCCESS.equals(catalogStatus);
        if (allSuccess && groups != null) {
            for (DdxGroupDetail group : groups) {
                if (!ConstantsUtility.MIRRORED_CATALOG_SUCCESS.equals(group.getGroupAddedStatus())
                        || !ConstantsUtility.MIRRORED_CATALOG_SUCCESS.equals(group.getGrantPermissionStatus())) {
                    allSuccess = false;
                    break;
                }
            }
        }

        return allSuccess ? ConstantsUtility.MIRRORED_CATALOG_SUCCESS : ConstantsUtility.MIRRORED_CATALOG_IN_PROGRESS;
    }

    private MirroredCatalogResponseVO buildMirroredCatalogResponse(DdxMirroredCatalogProduct data) {
        MirroredCatalogResponseVO response = new MirroredCatalogResponseVO();
        response.setDdxCorrelationId(data.getDdxCorrelationId());
        response.setStatus(data.getStatus());

        if (data.getMirrorCatalogDetails() != null) {
            MirroredCatalogDataVO catalogDataVO = new MirroredCatalogDataVO();
            catalogDataVO.setMirroredCatalogId(data.getMirrorCatalogDetails().getMirroredCatalogId());
            catalogDataVO.setCatalog(data.getCatalogName());
            catalogDataVO.setSchema(data.getSchemaName());
            catalogDataVO.setStorageAccountUrl(data.getStorageAccountUrl());
            catalogDataVO.setStatus(MirroredCatalogDataVO.StatusEnum.fromValue(data.getMirrorCatalogDetails().getCatalogStatus()));
            if (data.getObjects() != null) {
                catalogDataVO.setObjects(data.getObjects().stream()
                        .map(MirroredObjectDetail::getObjectName)
                        .collect(Collectors.toList()));
            }
            response.setDatabricksMirroredCatalog(catalogDataVO);
        }

        if (data.getDdxGroupDetails() != null && !data.getDdxGroupDetails().isEmpty()) {
            response.setDdxGroupDetails(mapDdxGroupDetailsToVO(data.getDdxGroupDetails()));
            DdxGroupDetail firstGroup = data.getDdxGroupDetails().get(0);
            GrantPermissionsVO grantPermissions = new GrantPermissionsVO();
            grantPermissions.setDdxGroup(firstGroup.getGroupName());
            grantPermissions.setStatus(firstGroup.getGrantPermissionStatus());
            response.setGrantPermissions(grantPermissions);
        }

        return response;
    }

    private List<DdxGroupDetailVO> mapDdxGroupDetailsToVO(List<DdxGroupDetail> groups) {
        if (groups == null) {
            return Collections.emptyList();
        }
        return groups.stream().map(g -> {
            DdxGroupDetailVO vo = new DdxGroupDetailVO();
            vo.setGroupName(g.getGroupName());
            vo.setGroupAddedStatus(g.getGroupAddedStatus());
            vo.setGrantPermissionStatus(g.getGrantPermissionStatus());
            vo.setTestRunId(g.getTestRunId());
            vo.setMessage(g.getMessage());
            return vo;
        }).collect(Collectors.toList());
    }

}
