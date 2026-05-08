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
import com.daimler.data.db.json.DdxProduct;
import com.daimler.data.db.json.Fabric2FabricDetail;
import com.daimler.data.db.json.GroupNameDetail;
import com.daimler.data.db.json.GroupNameList;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementCustomRepository;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementRepository;
import com.daimler.data.db.repo.ddxDataProductsDetails.DdxDataProductsDetailsRepository;
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
import com.daimler.data.dto.fabricWorkspace.LakehouseTableCollectionResponseVO;
import com.daimler.data.dto.fabricWorkspace.LakehouseColumnCollectionResponseVO;
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

import javax.swing.GroupLayout.Group;

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
            DdxDataProductsDetailsAssembler ddxDataProductsDetailsAssembler) {
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

            // Populate lakehouse table details with enabled status BEFORE saving
            populateLakehouseTableDetails(catalogMetadataDetails, existingFabricWorkspace, request);

            // Get the lakehouse ID to use as PK
            String lakehouseId = null;
            if (existingFabricWorkspace.getLakehouses() != null && !existingFabricWorkspace.getLakehouses().isEmpty()) {
                lakehouseId = existingFabricWorkspace.getLakehouses().get(0).getId();
            }
            if (lakehouseId == null || lakehouseId.isEmpty()) {
                log.warn("No lakehouse ID found, falling back to workspace ID");
                lakehouseId = existingFabricWorkspace.getId();
            }

            // Save metadata to repository using lakehouse ID as PK
            saveCatalogMetadata(request, catalogMetadataDetails, lakehouseId);

            // Save metadata to repository (now includes the populated lakehouse table details)
            saveCatalogMetadata(request, catalogMetadataDetails, existingFabricWorkspace.getId());

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
            
            // Retrieve stored metadata details (includes lakehouse table details from database)
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

            // Fetch existing entity to update
            FabricCatalogMetadataNsql entity = catalogCustomRepo.findByServiceName(request.getMetadata().getServiceName())
                .orElseThrow(() -> new EntityNotFoundException("Catalog metadata", request.getMetadata().getServiceName()));
            FabricCatalogMetadataDetailsVO vo = catalogAssembler.toVo(entity);
            
            // Update metadata fields
            vo.setMetadata(request.getMetadata());
            vo.setOwners(request.getOwners());
            vo.setMandatoryFields(request.getMandatoryFields());
            
            // Populate lakehouse table details with enabled status
            populateLakehouseTableDetails(vo, existingFabricWorkspace, request);
            
            // Save updated metadata with lakehouse table details
            catalogRepo.save(catalogAssembler.toEntity(vo));

            // Prepare success response
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
            FabricCatalogMetadataDetailsVO catalogMetadataDetails, String lakehouseId) {
        catalogMetadataDetails.setMetadata(request.getMetadata());
        catalogMetadataDetails.setOwners(request.getOwners());
        catalogMetadataDetails.setMandatoryFields(request.getMandatoryFields());
        FabricCatalogMetadataNsql entity = catalogAssembler.toEntity(catalogMetadataDetails);
        entity.setId(lakehouseId);
        catalogRepo.save(entity);
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
        
        // Retrieve lakehouse table details from stored data
        catalogMetadataDetails.setPublishedCdcTables(vo.getPublishedCdcTables());
        catalogMetadataDetails.setPublishedLakehouseTables(vo.getPublishedLakehouseTables());
        catalogMetadataDetails.setPublishedLakehouseTableDetails(vo.getPublishedLakehouseTableDetails());
        
        return catalogMetadataDetails;
    }

    private void populateLakehouseTableDetails(FabricCatalogMetadataDetailsVO catalogMetadataDetails, 
            FabricWorkspaceVO workspace, PublishCatalogRequestVO request) {
        try {
            // Get lakehouses from workspace
            if (workspace.getLakehouses() == null || workspace.getLakehouses().isEmpty()) {
                log.warn("No lakehouses found for workspace: {}", workspace.getId());
                return;
            }

            List<String> publishedCdcTables = new ArrayList<>();
            List<String> publishedLakehouseTables = new ArrayList<>();
            List<LakehouseTableDetailVO> publishedLakehouseTableDetails = new ArrayList<>();

            // Get published table names from the request metadata
            Set<String> requestedTableNames = new HashSet<>();
            Map<String, Set<String>> requestedColumnsByTable = new HashMap<>();
            
            if (request.getMetadata() != null && request.getMetadata().getDatabases() != null) {
                for (DatabaseMetadataVO db : request.getMetadata().getDatabases()) {
                    if (db.getSchemas() != null) {
                        for (SchemaMetadataVO schema : db.getSchemas()) {
                            if (schema.getTables() != null) {
                                for (TableMetadataVO table : schema.getTables()) {
                                    requestedTableNames.add(table.getTableName());
                                    publishedCdcTables.add(table.getTableName());
                                    
                                    Set<String> columnNames = new HashSet<>();
                                    if (table.getColumns() != null) {
                                        for (ColumnMetadataVO col : table.getColumns()) {
                                            columnNames.add(col.getColumnName());
                                        }
                                    }
                                    requestedColumnsByTable.put(table.getTableName(), columnNames);
                                }
                            }
                        }
                    }
                }
            }

            // Fetch all tables from the first lakehouse
            FabricLakehouseVO lakehouse = workspace.getLakehouses().get(0);
            String workspaceId = workspace.getId();
            String lakehouseId = lakehouse.getId();

            log.info("Fetching lakehouse tables for workspace: {} and lakehouse: {}", workspaceId, lakehouseId);
            
            // Get all tables from Fabric  
            var fabricTablesResponse = cdcPushServiceClient.getLakehouseTables(workspaceId, lakehouseId);
            
            if (fabricTablesResponse != null && fabricTablesResponse.getData() != null && 
                fabricTablesResponse.getData().getTables() != null) {
                
                for (var fabricTable : fabricTablesResponse.getData().getTables()) {
                    String tableName = fabricTable.getTableName();
                    publishedLakehouseTables.add(tableName);
                    
                    LakehouseTableDetailVO tableDetail = new LakehouseTableDetailVO();
                    tableDetail.setTableName(tableName);
                    
                    // Table is enabled if it's in the published (requested) tables
                    boolean isTableEnabled = requestedTableNames.contains(tableName);
                    tableDetail.setEnabled(isTableEnabled);
                    
                    // Get columns for this table
                    List<LakehouseColumnDetailVO> columnDetails = new ArrayList<>();
                    try {
                        var columnsResponse = cdcPushServiceClient.getTableSchema(workspaceId, lakehouseId, "dbo", tableName);
                        
                        if (columnsResponse != null && columnsResponse.getData() != null && 
                            columnsResponse.getData().getColumns() != null) {
                            
                            Set<String> requestedColumns = requestedColumnsByTable.getOrDefault(tableName, new HashSet<>());
                            
                            for (var fabricColumn : columnsResponse.getData().getColumns()) {
                                LakehouseColumnDetailVO columnDetail = new LakehouseColumnDetailVO();
                                columnDetail.setColumnName(fabricColumn.getColumnName());
                                
                                // Column is enabled if the table is enabled AND the column is in requested columns
                                boolean isColumnEnabled = isTableEnabled && requestedColumns.contains(fabricColumn.getColumnName());
                                columnDetail.setEnabled(isColumnEnabled);
                                
                                columnDetails.add(columnDetail);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch columns for table {}: {}", tableName, e.getMessage());
                    }
                    
                    tableDetail.setColumns(columnDetails);
                    publishedLakehouseTableDetails.add(tableDetail);
                }
            }

            // Set the populated lists to the response
            catalogMetadataDetails.setPublishedCdcTables(publishedCdcTables);
            catalogMetadataDetails.setPublishedLakehouseTables(publishedLakehouseTables);
            catalogMetadataDetails.setPublishedLakehouseTableDetails(publishedLakehouseTableDetails);
            
            log.info("Populated lakehouse table details: {} tables, {} published to CDC", 
                publishedLakehouseTables.size(), publishedCdcTables.size());
            
        } catch (Exception e) {
            log.error("Error populating lakehouse table details: {}", e.getMessage(), e);
            // Don't fail the whole operation, just log the error
        }
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

        Map<String, GroupNameList> existingGroups = checkForExistingGroupsInTheLakehouse(validGroups, dbLakehouseDetails);

        List<String> groupsToBeAdded = validGroups.stream()
                .filter(group -> (!existingGroups.containsKey(group)) || (existingGroups.containsKey(group)
                        && ConstantsUtility.GROUPS_NOT_FOUND_CONSTANT.equals(existingGroups.get(group).getStatus())))
                .collect(Collectors.toList());
        if (groupsToBeAdded.isEmpty()) {
            log.info("All groups: {} are already added to the lakehouse: {}", validGroups, lakehouseName);
            return;
        }

        Fabric2FabricDetail fabric2FabricDetail = new Fabric2FabricDetail(); 
        fabric2FabricDetail.setInitiatedOn(new Date());
        fabric2FabricDetail.setIsFabric2Fabric(Boolean.TRUE);
        fabric2FabricDetail.setGroupsNames(new ArrayList<>());

        GroupNameDetail groupNameDetail = new GroupNameDetail();
        groupNameDetail.setGroupNameList(buildGroupStatusList(groupsToBeAdded, ConstantsUtility.GROUPS_IN_PROGRESS_CONSTANT));
        
        // Adding the TRS user in the workspace to make sure the user has access to the lakehouse where the groups are being added. 
        // This is required as part of the UIlicious test case which adds the groups to the lakehouse and needs access to the lakehouse.
        try{
            fabricWorkspaceClient.addUser(workspaceId, pidUser);
            log.info("Added user: {} to workspace: {} to facilitate UIlicious test case execution for adding groups to lakehouse", pidUser, workspaceName);
        } catch(Exception e){
            log.error("Exception while adding the user to the workspace : {} with exception", workspaceName, e.getMessage());
        }

        String testRunID = null;
        try{
            testRunID = this.uiLiciousClient.addWorkspaceGroupsToLakehouse(workspaceId, lakehouseId, workspaceName, lakehouseName, groupsToBeAdded);
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

            boolean allRecordsCompleted = true;
            for(Fabric2FabricDetail fabric2FabricDetails : ddxProduct.getFabric2fabricDetails()){
                for(GroupNameDetail groupNameDetail : fabric2FabricDetails.getGroupsNames()){
                    if(groupNameDetail.getRunStatus().equals(ConstantsUtility.GROUPS_COMPLETED_CONSTANT)){
                        continue;
                    }
                    
                    List<GroupStatusResponseVO> groupStatusList = this.uiLiciousClient.getStatusOfGroupsAdditionToLakehouse(workspaceName, workspaceId, lakehouseName, lakehouseId, validGroups, groupNameDetail.getTestRunId());
                    
                    if(groupStatusList != null && !groupStatusList.isEmpty()){
                        log.info("Group status for lakehouseId {}: {}", lakehouseId, groupStatusList);
                        Map<String, String> statusMap = groupStatusList.stream().collect(Collectors.toMap(GroupStatusResponseVO::getGroupName, GroupStatusResponseVO::getStatus));
                        groupNameDetail.getGroupNameList().forEach(group->{
                            group.setStatus(statusMap.get(group.getGroupName()));
                            group.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(group.getStatus()));
                        });
                        groupNameDetail.setRunStatus(ConstantsUtility.GROUPS_COMPLETED_CONSTANT);
                    }
                    
                    if(!ConstantsUtility.GROUPS_COMPLETED_CONSTANT.equals(groupNameDetail.getRunStatus())){
                        allRecordsCompleted = false;
                    }
                }
            }
            
            List<GroupStatusResponseVO> finalGroupStatusList = new ArrayList<>();
            Map<String, GroupNameList> groupsMap = checkForExistingGroupsInTheLakehouse(validGroups, dbEntity);
            for(String group: validGroups){
                GroupStatusResponseVO response = new GroupStatusResponseVO();
                response.setGroupName(group);
                if(groupsMap.containsKey(group)){
                    GroupNameList tempGroup = groupsMap.get(group);
                    response.setStatus(tempGroup.getStatus());
                    response.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(tempGroup.getStatus()));
                } else {
                    response.setStatus(ConstantsUtility.GROUPS_UNKNOWN_CONSTANT);
                    response.setMessage(ConstantsUtility.GROUPES_ERROR_MESSAGES_CONSTANT_MAP.get(ConstantsUtility.GROUPS_UNKNOWN_CONSTANT));
                }
                finalGroupStatusList.add(response);
            }

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
                try{
                    this.fabricWorkspaceClient.removeUserGroup(workspaceId, pidUserIdentifier);
                } catch(Exception e){
                    log.error("Exception while removing the user from the workspace : {} with exception", workspaceName, e.getMessage());
                }
            }
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
        try{
            fabricWorkspaceClient.addUser(workspace.getId(), pidUser);
            log.info("Added user: {} to workspace: {} to facilitate UIlicious test case execution for adding groups to lakehouse", pidUser, workspaceName);
        } catch(Exception e){
            log.error("Exception while adding the user to the workspace : {} with exception", workspaceName, e.getMessage());
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

    private Map<String, GroupNameList> checkForExistingGroupsInTheLakehouse(List<String> groupsName, DdxDataProductsDetailsNsql ddxDataProductsDetailsNsql){
        Map<String, GroupNameList> existingGroups = new HashMap<>();
        Map<String, GroupNameList> groupNameListMap = new HashMap<>();
        if(ddxDataProductsDetailsNsql.getData() != null && ddxDataProductsDetailsNsql.getData().getDdxProducts() != null){
            for(DdxProduct ddx: ddxDataProductsDetailsNsql.getData().getDdxProducts()){
                if(ddx.getFabric2fabricDetails() != null){
                    for(Fabric2FabricDetail fabric2FabricDetail : ddx.getFabric2fabricDetails()){
                        if(fabric2FabricDetail.getGroupsNames() != null){
                            for(GroupNameDetail groupNameDetail : fabric2FabricDetail.getGroupsNames()){
                                if(groupNameDetail.getGroupNameList() != null){
                                    for(GroupNameList groupNameList : groupNameDetail.getGroupNameList()){
                                        if(!groupNameListMap.containsKey(groupNameList.getGroupName())){
                                            groupNameListMap.put(groupNameList.getGroupName(), groupNameList);
                                        } else if(groupNameListMap.containsKey(groupNameList.getGroupName())){
                                            if(ConstantsUtility.GROUPS_ADDED_CONSTANT.equals(groupNameListMap.get(groupNameList.getGroupName()).getStatus()) && !ConstantsUtility.GROUPS_ADDED_CONSTANT.equals(groupNameList.getStatus())){
                                                continue;
                                            } else if(ConstantsUtility.GROUPS_IN_PROGRESS_CONSTANT.equals(groupNameListMap.get(groupNameList.getGroupName()).getStatus()) && !ConstantsUtility.GROUPS_ADDED_CONSTANT.equals(groupNameList.getStatus())){
                                                continue;
                                            }
                                            groupNameListMap.put(groupNameList.getGroupName(), groupNameList);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for(String groupName : groupsName){
            if(groupNameListMap.containsKey(groupName)){
                existingGroups.put(groupName, groupNameListMap.get(groupName));
            }
        }
        return existingGroups;
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
    public TableMismatchResponseVO checkTableMismatch(String workspaceId, String lakehouseId, String serviceName) {
        log.info("Checking table mismatch for workspace: {}, lakehouse: {}", workspaceId, lakehouseId);
        TableMismatchResponseVO response = new TableMismatchResponseVO();
        List<TableMismatchDetailVO> mismatches = new ArrayList<>();
        try {
            FabricCatalogMetadataVO storedMetadata = retrieveMetadataFromOpenMetadata(serviceName);

            if (storedMetadata == null
                    || storedMetadata.getDatabases() == null
                    || storedMetadata.getDatabases().isEmpty()) {
                log.info("First CDC push detected - skipping mismatch check for service: {}", serviceName);
                response.setHasMismatch(false);
                response.setMismatches(new ArrayList<>());
                GenericMessage msg = new GenericMessage();
                msg.setSuccess(SUCCESS_STATUS);
                response.setResponses(msg);
                return response;
            }

            LakehouseTableCollectionResponseVO fabricTables = cdcPushServiceClient.getLakehouseTables(workspaceId,
                    lakehouseId);

            if (fabricTables == null
                    || fabricTables.getData() == null
                    || fabricTables.getData().getTables() == null) {
                log.warn("No tables returned from Fabric for workspace: {}, lakehouse: {}",
                        workspaceId, lakehouseId);
                response.setHasMismatch(false);
                response.setMismatches(mismatches);
                GenericMessage msg = new GenericMessage();
                msg.setSuccess(SUCCESS_STATUS);
                response.setResponses(msg);
                return response;
            }

            Map<String, TableMetadataVO> storedTableMap = new HashMap<>();

            for (DatabaseMetadataVO db : storedMetadata.getDatabases()) {
                if (db.getSchemas() != null) {

                    for (SchemaMetadataVO schema : db.getSchemas()) {
                        if (schema.getTables() != null) {

                            for (TableMetadataVO table : schema.getTables()) {
                                storedTableMap.put(table.getTableName(), table);
                            }
                        }
                    }
                }
            }

            Set<String> fabricTableNames = new HashSet<>();
            Map<String, com.daimler.data.dto.fabricWorkspace.LakeHouseTableVO> fabricTableMap = new HashMap<>();
            for (com.daimler.data.dto.fabricWorkspace.LakeHouseTableVO fabricTable : fabricTables.getData()
                    .getTables()) {
                fabricTableNames.add(fabricTable.getTableName());
                fabricTableMap.put(fabricTable.getTableName(), fabricTable);
            }

            for (String fabricTableName : fabricTableNames) {

                if (!storedTableMap.containsKey(fabricTableName)) {
                    TableMismatchDetailVO detail = new TableMismatchDetailVO();
                    detail.setTableName(fabricTableName);
                    detail.setMismatchType(TableMismatchDetailVO.MismatchTypeEnum.NEW_TABLE);
                    detail.setDetails("Table exists in Fabric but not in published CDC metadata");
                    mismatches.add(detail);
                }
            }

            for (String storedTableName : storedTableMap.keySet()) {

                if (!fabricTableNames.contains(storedTableName)) {
                    TableMismatchDetailVO detail = new TableMismatchDetailVO();
                    detail.setTableName(storedTableName);
                    detail.setMismatchType(TableMismatchDetailVO.MismatchTypeEnum.DELETED_TABLE);
                    detail.setDetails("Table exists in published CDC metadata but no longer in Fabric");
                    mismatches.add(detail);
                }
            }

            for (String tableName : storedTableMap.keySet()) {
                if (fabricTableNames.contains(tableName)) {
                    TableMetadataVO storedTable = storedTableMap.get(tableName);
                    compareTableColumns(
                            workspaceId,
                            lakehouseId,
                            tableName,
                            storedTable,
                            mismatches);
                }
            }

            response.setHasMismatch(!mismatches.isEmpty());
            response.setMismatches(mismatches);
            GenericMessage successMsg = new GenericMessage();
            successMsg.setSuccess(SUCCESS_STATUS);
            response.setResponses(successMsg);
        } catch (Exception e) {
            log.error("Error checking table mismatch for workspace {}: {}",
                    workspaceId, e.getMessage(), e);
            response.setHasMismatch(false);
            response.setMismatches(mismatches);
            GenericMessage errorMsg = new GenericMessage();
            errorMsg.setSuccess(FAILED_STATUS);
            MessageDescription message = new MessageDescription();
            message.setMessage("Failed to check table mismatch: " + e.getMessage());
            errorMsg.addErrors(message);
            response.setResponses(errorMsg);
        }
        return response;
    }

    private void compareTableColumns(String workspaceId, String lakehouseId, String tableName,
            TableMetadataVO storedTable, List<TableMismatchDetailVO> mismatches) {
        try {
            // Fetch current column details from Fabric
            LakehouseColumnCollectionResponseVO fabricColumns = cdcPushServiceClient.getTableSchema(
                    workspaceId, lakehouseId, "dbo", tableName);

            if (fabricColumns == null || fabricColumns.getData() == null
                    || fabricColumns.getData().getColumns() == null) {
                return;
            }

            // Build maps for comparison
            Map<String, String> storedColumnTypes = new HashMap<>();
            if (storedTable.getColumns() != null) {
                for (ColumnMetadataVO col : storedTable.getColumns()) {
                    storedColumnTypes.put(col.getColumnName(), col.getColType());
                }
            }

            Map<String, String> fabricColumnTypes = new HashMap<>();
            for (com.daimler.data.dto.fabricWorkspace.LakehouseColumnVO col : fabricColumns.getData().getColumns()) {
                fabricColumnTypes.put(col.getColumnName(), col.getColType());
            }

            // Detect added columns
            List<String> addedColumns = new ArrayList<>();
            for (String fabricColName : fabricColumnTypes.keySet()) {
                if (!storedColumnTypes.containsKey(fabricColName)) {
                    addedColumns.add(fabricColName);
                }
            }
            if (!addedColumns.isEmpty()) {
                TableMismatchDetailVO detail = new TableMismatchDetailVO();
                detail.setTableName(tableName);
                detail.setMismatchType(TableMismatchDetailVO.MismatchTypeEnum.COLUMNS_ADDED);
                detail.setDetails("New columns found in Fabric: " + String.join(", ", addedColumns));
                detail.setAffectedColumns(addedColumns);
                mismatches.add(detail);
            }

            // Detect removed columns
            List<String> removedColumns = new ArrayList<>();
            for (String storedColName : storedColumnTypes.keySet()) {
                if (!fabricColumnTypes.containsKey(storedColName)) {
                    removedColumns.add(storedColName);
                }
            }
            if (!removedColumns.isEmpty()) {
                TableMismatchDetailVO detail = new TableMismatchDetailVO();
                detail.setTableName(tableName);
                detail.setMismatchType(TableMismatchDetailVO.MismatchTypeEnum.COLUMNS_REMOVED);
                detail.setDetails("Columns removed from Fabric: " + String.join(", ", removedColumns));
                detail.setAffectedColumns(removedColumns);
                mismatches.add(detail);
            }

            // Detect column type changes
            List<String> typeChangedColumns = new ArrayList<>();
            for (String colName : storedColumnTypes.keySet()) {
                if (fabricColumnTypes.containsKey(colName)) {
                    String storedType = storedColumnTypes.get(colName);
                    String fabricType = fabricColumnTypes.get(colName);
                    if (storedType != null && fabricType != null && !storedType.equalsIgnoreCase(fabricType)) {
                        typeChangedColumns.add(colName);
                    }
                }
            }
            if (!typeChangedColumns.isEmpty()) {
                TableMismatchDetailVO detail = new TableMismatchDetailVO();
                detail.setTableName(tableName);
                detail.setMismatchType(TableMismatchDetailVO.MismatchTypeEnum.COLUMN_TYPE_CHANGED);
                detail.setDetails("Column types changed in Fabric: " + String.join(", ", typeChangedColumns));
                detail.setAffectedColumns(typeChangedColumns);
                mismatches.add(detail);
            }

        } catch (Exception e) {
            log.error("Error comparing columns for table {}: {}", tableName, e.getMessage());
        }
    }
}
