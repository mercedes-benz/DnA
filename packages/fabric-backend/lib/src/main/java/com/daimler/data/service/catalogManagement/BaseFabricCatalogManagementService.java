package com.daimler.data.service.catalogManagement;

import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.assembler.FabricCatalogMetadataAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.*;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementCustomRepository;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.CdcPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;
import com.daimler.data.dto.fabricCatalogManagement.*;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.OpenMetadataFqnBuilder;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class BaseFabricCatalogManagementService extends BaseCommonService<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql, String> 
    implements FabricCatalogManagementService {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";
    private static final String CONFLICT_STATUS = "CONFLICT";
    private static final String NOT_FOUND_STATUS = "NOT_FOUND";

    private final FabricWorkspaceCustomRepository customRepo;
    private final FabricWorkspaceRepository jpaRepo;
    private final FabricWorkspaceAssembler assembler;
    private final OpenMetadataClient openMetadataClient;
    private final FabricCatalogManagementRepository catalogRepo;
    private final FabricCatalogManagementCustomRepository catalogCustomRepo;
    private final FabricCatalogMetadataAssembler catalogAssembler;
	

    @Autowired
    public BaseFabricCatalogManagementService(
            FabricWorkspaceCustomRepository customRepo,
            FabricWorkspaceRepository jpaRepo,
            FabricWorkspaceAssembler assembler,
            OpenMetadataClient openMetadataClient,
            FabricCatalogManagementRepository catalogRepo,
            FabricCatalogManagementCustomRepository catalogCustomRepo,
            FabricCatalogMetadataAssembler catalogAssembler) {
        this.customRepo = customRepo;
        this.jpaRepo = jpaRepo;
        this.assembler = assembler;
        this.openMetadataClient = openMetadataClient;
        this.catalogRepo = catalogRepo;
        this.catalogCustomRepo = catalogCustomRepo;
        this.catalogAssembler = catalogAssembler;
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
                existingFabricWorkspace.getDescription());

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
}