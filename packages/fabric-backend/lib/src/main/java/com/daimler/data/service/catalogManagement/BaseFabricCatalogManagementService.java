package com.daimler.data.service.catalogManagement;

import java.util.List;
import java.util.ArrayList;
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
import com.daimler.data.dto.fabricCatalogManagement.PublishCatalogRequestVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.DatabaseMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.SchemaMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.TableMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.ColumnMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.CreatedByVO;
import com.daimler.data.dto.fabricWorkspace.FabricWorkspaceVO;
import com.daimler.data.service.common.BaseCommonService;

import org.openmetadata.client.model.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseFabricCatalogManagementService extends
		BaseCommonService<FabricWorkspaceVO, FabricWorkspaceNsql, String> implements FabricCatalogManagementService {

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

	public GenericMessage publishCatalogMetaData(PublishCatalogRequestVO request) {
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
					metadata.getServiceName(),
					ownerReferences);

			for (DatabaseMetadataVO dbMetadata : metadata.getDatabases()) {
				Database database = openMetadataClient.createDatabase(
						dbMetadata.getDbName(),
						metadata.getServiceName());

				for (SchemaMetadataVO schemaMetadata : dbMetadata.getSchemas()) {
					DatabaseSchema schema = openMetadataClient.createSchema(
							schemaMetadata.getSchemaName(),
							metadata.getServiceName() + "." + database.getName());

					for (TableMetadataVO tableMetadata : schemaMetadata.getTables()) {
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
								metadata.getServiceName() + "." + database.getName() + "." + schema.getName(),
								columns);
					}
				}
			}	

		} catch (EntityAlreadyExistsException e) {
			return createErrorResponse("CONFLICT",
					"Catalog metadata already exists. Error: " + e.getMessage());
		} catch (OpenMetadataClientException e) {
			return createErrorResponse("FAILED",
					"Failed to publish catalog: " + e.getMessage());
		}
		response.setSuccess("SUCCESS");
			return response;

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
