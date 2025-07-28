package com.daimler.data.service.catalogManagement;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.application.client.OpenMetadataClient;
import com.daimler.data.assembler.FabricWorkspaceAssembler;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
import com.daimler.data.controller.exceptions.EntityAlreadyExistsException;
import com.daimler.data.controller.exceptions.OpenMetadataClientException;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.FabricWorkspaceNsql;
import com.daimler.data.db.repo.fabric.FabricWorkspaceCustomRepository;
import com.daimler.data.db.repo.fabric.FabricWorkspaceRepository;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
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
public class BaseFabricCatalogManagementService extends BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricCatalogManagementService {

	@Autowired
	private FabricWorkspaceCustomRepository customRepo;

	@Autowired
	private FabricWorkspaceRepository jpaRepo;

	@Autowired
	private FabricWorkspaceAssembler assembler;

	@Autowired
	private OpenMetadataClient openMetadataClient;

	public BaseFabricCatalogManagementService() {
		super();
	}

	@Override
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

		existingFabricWorkspace.setCdcLakeHouseDetails(metadata);
		jpaRepo.save(assembler.toEntity(existingFabricWorkspace));

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
