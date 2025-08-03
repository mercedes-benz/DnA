package com.daimler.data.service.catalogManagement;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.assembler.FabricCatalogMetadataAssembler;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
import com.daimler.data.controller.exceptions.EntityAlreadyExistsException;
import com.daimler.data.controller.exceptions.OpenMetadataClientException;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.repo.catalogManagement.FabricCatalogManagementRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricWorkspace.CdcPublishedLakeHouseDetailsVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataDetailsVO;
import com.daimler.data.dto.fabricCatalogManagement.DatabaseMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.SchemaMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.TableMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO;
import com.daimler.data.dto.fabricCatalogManagement.ColumnMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.CreatedByVO;
import com.daimler.data.dto.fabricCatalogManagement.CreatedByVO;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.OpenMetadataFqnBuilder;

import org.openmetadata.client.model.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseFabricCatalogManagementService extends BaseCommonService<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql, String> implements FabricCatalogManagementService {

	@Autowired
	private FabricWorkspaceCustomRepository customRepo;

	@Autowired
	private FabricWorkspaceRepository jpaRepo;

	@Autowired
	private FabricWorkspaceAssembler assembler;

	@Autowired
	private OpenMetadataClient openMetadataClient;

	@Autowired
	private FabricCatalogManagementRepository catalogRepo;

	@Autowired
	private FabricCatalogMetadataAssembler catalogAssembler;

	public BaseFabricCatalogManagementService() {
		super();
	}

	@Override
	@Transactional
	public GenericMessage publishCatalogMetaData(PublishCatalogRequestVO request, FabricWorkspaceVO existingFabricWorkspace) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> messages = new ArrayList<>();
		List<MessageDescription> warningMessages = new ArrayList<>();
		MessageDescription message = new MessageDescription();

		FabricCatalogMetadataVO metadata = request.getMetaData();
		List<CreatedByVO> owners = request.getOwners();

		List<EntityReference> ownerReferences = new ArrayList<>();
		for (CreatedByVO owner : owners) {
			try {
				User user = openMetadataClient.getUserByFqn(owner.getId());
				ownerReferences.add(openMetadataClient.createEntityReference(user));
			} catch (EntityNotFoundException e) {
				MessageDescription warning = new MessageDescription();
				warning.setMessage("User " + owner.getId()
						+ " not found in OpenMetadata. Please ensure they've logged in to CDC.");
				warningMessages.add(warning);
				owners.remove(owner);

			}
		}

		if (ownerReferences.isEmpty()) {
			return createErrorResponse("FAILED", "No valid owners found. At least one valid owner required.");
		}

		if (!warningMessages.isEmpty()) {
			response.setWarnings(warningMessages);
		}

		try {
			DatabaseService databaseService = openMetadataClient.createDatabaseService(
					existingFabricWorkspace.getName(),
					ownerReferences);

			for (DatabaseMetadataVO dbMetadata : metadata.getDatabases()) {
				Database database = openMetadataClient.createDatabase(
						dbMetadata.getDbName(),
						existingFabricWorkspace.getName(), request.getMandatoryFields());
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

		//Adding CDC published lake house details
		CdcPublishedLakeHouseDetailsVO cdcPublishedLakeHouseDetails = new CdcPublishedLakeHouseDetailsVO();
		List<String> lakeHouseNames = metadata.getDatabases().stream()
                                      .map(DatabaseMetadataVO::getDbName)
                                      .collect(Collectors.toList());
		cdcPublishedLakeHouseDetails.setIsLakeHousesPublishedToCdc(true);
		cdcPublishedLakeHouseDetails.setPublishedLakeHouseNames(lakeHouseNames);
		existingFabricWorkspace.setCdcPublishedLakeHouseDetails(cdcPublishedLakeHouseDetails);
		jpaRepo.save(assembler.toEntity(existingFabricWorkspace));

		//Adding metadata to FabricCatalogMetadataNsql
		FabricCatalogMetadataDetailsVO catalogMetadataDetails = new FabricCatalogMetadataDetailsVO();
		catalogMetadataDetails.setMetadata(metadata);
		catalogMetadataDetails.setOwners(owners);
		catalogMetadataDetails.setMandatoryFields(request.getMandatoryFields());
		
		catalogRepo.save(catalogAssembler.toEntity(catalogMetadataDetails));

		} catch (EntityAlreadyExistsException e) {
			return createErrorResponse("CONFLICT",
					"Catalog metadata already exists. Error: " + e.getMessage());
		} catch (OpenMetadataClientException e) {
			return createErrorResponse("FAILED",
					"Failed to publish catalog: " + e.getMessage());
		} catch ( Exception e) {
			return createErrorResponse("FAILED",
					"Failed to publish catalog: " + e.getMessage());
		}

		response.setSuccess("SUCCESS");
		return response;

	}

	@Override
	public FabricCatalogMetadataVO getCatalogMetadata(String serviceName) {
		FabricCatalogMetadataVO metadata = new FabricCatalogMetadataVO();
		metadata.setServiceName(serviceName);
		try {
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

		} catch (EntityNotFoundException e) {
			throw new EntityNotFoundException("Metadata details", serviceName);
		} catch (Exception e) {
			throw new OpenMetadataClientException("Failed to get catalog metadata for workspace: " + 
				serviceName + " " + e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public GenericMessage updateCatalogMetaData(PublishCatalogRequestVO request, FabricWorkspaceVO existingFabricWorkspace) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> warningMessages = new ArrayList<>();

		// 1. Validate owners
		List<EntityReference> ownerReferences = validateOwners(request.getOwners(), warningMessages);
		if (ownerReferences.isEmpty()) {
			return createErrorResponse("FAILED", "No valid owners found. At least one valid owner required.");
		}

		if (!warningMessages.isEmpty()) {
			response.setWarnings(warningMessages);
		}

		try {
			// 2. Get existing metadata for comparison
			FabricCatalogMetadataVO existingMetadata = getCatalogMetadata(existingFabricWorkspace.getName());
			
			// 3. Get existing service
			DatabaseService service = openMetadataClient.getDatabaseService(existingFabricWorkspace.getName());
			
			// 4. Update service owners if changed
			updateServiceOwners(service, ownerReferences);

			// 5. Process deletions first (bottom-up: columns -> tables -> schemas -> databases)
			handleDeletions(existingMetadata, request.getMetaData());

			// 6. Process updates and additions - THIS IS WHERE WE CALL UPDATE METHODS
			processUpdates(request, existingFabricWorkspace.getName());

			// 7. Update CDC lake house details
			updateLakeHouseDetails(existingFabricWorkspace, request.getMetaData());

			// 8. Update stored metadata
			updateStoredMetadata(request);

			response.setSuccess("SUCCESS");
		} catch (EntityNotFoundException e) {
			return createErrorResponse("NOT_FOUND", e.getMessage());
		} catch (OpenMetadataClientException | EntityAlreadyExistsException e) {
			return createErrorResponse("FAILED", e.getMessage());
		} catch (Exception e) {
			return createErrorResponse("FAILED", "Unexpected error: " + e.getMessage());
		}

		return response;
	}

	private void handleDeletions(FabricCatalogMetadataVO existingMetadata, FabricCatalogMetadataVO newMetadata) {
		// 1. Find and delete removed databases (present in existing but not in new)
		List<DatabaseMetadataVO> deletedDbs = existingMetadata.getDatabases().stream()
			.filter(existingDb -> existingDb.getDbId() != null) // Only check databases that exist in OpenMetadata
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
			// Skip databases that were already deleted
			if (existingDb.getDbId() == null || deletedDbs.contains(existingDb)) {
				continue;
			}
			
			// Find matching database in new metadata
			Optional<DatabaseMetadataVO> matchingNewDb = newMetadata.getDatabases().stream()
				.filter(newDb -> newDb.getDbId() != null && newDb.getDbId().equals(existingDb.getDbId()))
				.findFirst();
				
			if (matchingNewDb.isPresent()) {
				DatabaseMetadataVO newDb = matchingNewDb.get();
				
				// Find schemas that were deleted
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
					// Skip schemas that were already deleted or don't have IDs
					if (existingSchema.getSchemaId() == null || deletedSchemas.contains(existingSchema)) {
						continue;
					}
					
					// Find matching schema in new metadata
					Optional<SchemaMetadataVO> matchingNewSchema = newDb.getSchemas().stream()
						.filter(newSchema -> newSchema.getSchemaId() != null && 
								newSchema.getSchemaId().equals(existingSchema.getSchemaId()))
						.findFirst();
						
					if (matchingNewSchema.isPresent()) {
						SchemaMetadataVO newSchema = matchingNewSchema.get();
						
						// Find tables that were deleted
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
							// Skip tables that were already deleted or don't have IDs
							if (existingTable.getTableId() == null || deletedTables.contains(existingTable)) {
								continue;
							}
							
							// Find matching table in new metadata
							Optional<TableMetadataVO> matchingNewTable = newSchema.getTables().stream()
								.filter(newTable -> newTable.getTableId() != null && 
										newTable.getTableId().equals(existingTable.getTableId()))
								.findFirst();
								
							if (matchingNewTable.isPresent()) {
								TableMetadataVO newTable = matchingNewTable.get();
								
								// Find columns that were deleted
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
						}
					}
				}
			}
		}
	}

	private void processUpdates(PublishCatalogRequestVO request, String serviceName) {
		// Update or create all databases
		for (DatabaseMetadataVO dbMetadata : request.getMetaData().getDatabases()) {
			// This will call the updateDatabase method
			updateDatabase(dbMetadata, serviceName, request.getMandatoryFields());
		}
	}
	
	private void updateStoredMetadata(PublishCatalogRequestVO request) {
		// Find existing metadata (implementation depends on your repository)
		FabricCatalogMetadataDetailsVO details = catalogRepo.findAll().stream()
			.map(catalogAssembler::toVo)
			.filter(vo -> vo.getMetadata().getServiceName().equals(request.getMetaData().getServiceName()))
			.findFirst()
			.orElse(new FabricCatalogMetadataDetailsVO());

		// Update all fields
		details.setMetadata(request.getMetaData());
		details.setOwners(request.getOwners());
		details.setMandatoryFields(request.getMandatoryFields());
		
		catalogRepo.save(catalogAssembler.toEntity(details));
	}

	// Helper methods
	private void updateLakeHouseDetails(FabricWorkspaceVO workspace, FabricCatalogMetadataVO metadata) {
		CdcPublishedLakeHouseDetailsVO details = workspace.getCdcPublishedLakeHouseDetails();
		if (details == null) {
			details = new CdcPublishedLakeHouseDetailsVO();
		}
		
		details.setIsLakeHousesPublishedToCdc(true);
		details.setPublishedLakeHouseNames(
			metadata.getDatabases().stream()
				.map(DatabaseMetadataVO::getDbName)
				.collect(Collectors.toList()));
		
		workspace.setCdcPublishedLakeHouseDetails(details);
		jpaRepo.save(assembler.toEntity(workspace));
	}

	
	private List<EntityReference> validateOwners(List<CreatedByVO> owners, List<MessageDescription> warningMessages) {
		List<EntityReference> ownerReferences = new ArrayList<>();
		for (CreatedByVO owner : new ArrayList<>(owners)) {
			try {
				User user = openMetadataClient.getUserByFqn(owner.getId());
				ownerReferences.add(openMetadataClient.createEntityReference(user));
			} catch (EntityNotFoundException e) {
				MessageDescription warning = new MessageDescription();
				warning.setMessage("User " + owner.getId() + " not found in OpenMetadata. Please ensure they've logged in to CDC.");
				warningMessages.add(warning);
				owners.remove(owner);
			}
		}
		return ownerReferences;
	}

	private void updateServiceOwners(DatabaseService service, List<EntityReference> newOwners) {
		CreateDatabaseService updateRequest = new CreateDatabaseService()
			.name(service.getName())
			.serviceType(CreateDatabaseService.ServiceTypeEnum.DATALAKE)
			.connection(service.getConnection())
			.owners(newOwners);
		
		openMetadataClient.updateDatabaseService(updateRequest);
	}

	private void updateDatabase(DatabaseMetadataVO dbMetadata, String serviceName, MandatoryFieldsVO fields) {
		try {
			Database database;
			
			if (dbMetadata.getDbId() != null) {
				// Existing database - update it
				database = openMetadataClient.updateDatabase(
					dbMetadata.getDbId(),
					dbMetadata.getDbName(),
					serviceName,
					fields);
			} else {
				// New database - create it
				database = openMetadataClient.createDatabase(
					dbMetadata.getDbName(),
					serviceName,
					fields);
				
				// Set the new ID back to the metadata
				dbMetadata.setDbId(database.getId().toString());
				
				// Get the full database entity with service reference
				database = openMetadataClient.getDatabaseById(database.getId().toString());
			}

			// Process schemas using the actual database object (not just the metadata)
			for (SchemaMetadataVO schemaMetadata : dbMetadata.getSchemas()) {
				updateSchema(schemaMetadata, database); // Pass the actual database object
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

			// Process tables using the actual schema object
			for (TableMetadataVO tableMetadata : schemaMetadata.getTables()) {
				updateTable(tableMetadata, schema, database); // Pass both schema and database
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
		List<MessageDescription> messages = new ArrayList<>();
		msg.setMessage(message);
		messages.add(msg);
		response.setErrors(messages);
		response.setSuccess(status);
		return response;
	}	
}
