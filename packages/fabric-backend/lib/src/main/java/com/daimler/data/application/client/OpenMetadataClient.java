package com.daimler.data.application.client;

import com.daimler.data.controller.exceptions.OpenMetadataClientException;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
import com.daimler.data.controller.exceptions.EntityAlreadyExistsException;
import com.daimler.data.dto.fabricCatalogManagement.MandatoryFieldsVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.client.ApiClient;
import org.openmetadata.client.api.*;
import org.openmetadata.client.model.*;
import org.openmetadata.schema.services.connections.database.DatalakeConnection;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import feign.FeignException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMetadataClient {

    private final ApiClient apiClient;

// get methods

    public User getUserByFqn(String username) {
        try {
            return apiClient.buildClient(UsersApi.class)
                    .getUserByFQN(username, null, null);
        } catch (Exception e) {
            throw new EntityNotFoundException("User", username);
        }
    }

    public DatabaseService getDatabaseService(String name) {
        try {
            return apiClient.buildClient(DatabaseServicesApi.class)
                    .getDatabaseServiceByFQN(name, null, null);
        }catch (Exception e) {
            throw new EntityNotFoundException("DatabaseService", name);
        }
    }
    
     public Database getDatabase(String serviceName, String dbName) {
        try {
            String fqn = serviceName + "." + dbName;
            return apiClient.buildClient(DatabasesApi.class)
                    .getDatabaseByFQN(fqn, null, null);
        } catch (Exception e) {
            throw new EntityNotFoundException("Database", serviceName + "." + dbName);
        }
    }

    public DatabaseSchema getSchema(String dbFQN, String schemaName) {
        try {
            String fqn = dbFQN + "." + schemaName;
            return apiClient.buildClient(DatabaseSchemasApi.class)
                    .getDBSchemaByFQN(fqn, null, null);
        } catch (Exception e) {
            throw new EntityNotFoundException("Schema", dbFQN + "." + schemaName);
        }
    }

    public Table getTable(String schemaFQN, String tableName) {
        try {
            String fqn = schemaFQN + "." + tableName;
            return apiClient.buildClient(TablesApi.class)
                    .getTableByFQN(fqn, null, null);
        } catch (Exception e) {
            throw new EntityNotFoundException("Table", schemaFQN + "." + tableName);
        }
    }

// Create methods

    public DatabaseService createDatabaseService(String name, List<EntityReference> owners) {
        try {
            CreateDatabaseService request = new CreateDatabaseService()
                    .name(name)
                    .serviceType(CreateDatabaseService.ServiceTypeEnum.DATALAKE);

            DatabaseConnection connection = new DatabaseConnection();
            connection.setConfig(new DatalakeConnection()
                    .withSupportsMetadataExtraction(true));
            request.setConnection(connection);
            request.setOwners(owners);

            return apiClient.buildClient(DatabaseServicesApi.class)
                    .createDatabaseService(request);
         } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("DatabaseService already Exists "+ name, e);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create DatabaseService: " + name, e);
        }
    }

    public Database createDatabase(String name, String serviceFQN, MandatoryFieldsVO fields) {
        try {
            CreateDatabase request = new CreateDatabase()
                    .name(name)
                    .service(serviceFQN)
                    .extension(toExtensions(fields)); // Using FQN directly as string

            return apiClient.buildClient(DatabasesApi.class)
                    .createOrUpdateDatabase(request);
        } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("Database already exists: " + name, e);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Database: " + name, e);
        }
    }

    public DatabaseSchema createSchema(String name, String dbFQN) {
        try {
            CreateDatabaseSchema request = new CreateDatabaseSchema()
                    .name(name)
                    .database(dbFQN);

            return apiClient.buildClient(DatabaseSchemasApi.class)
                    .createOrUpdateDBSchema(request);
        } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("Schema already exists: " + name, e);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Schema: " + name, e);
        }
    }

    public Table createTable(String name, String schemaFQN, List<Column> columns) {
        try {
            CreateTable request = new CreateTable()
                    .name(name)
                    .databaseSchema(schemaFQN)
                    .columns(columns);

            return apiClient.buildClient(TablesApi.class)
                    .createOrUpdateTable(request);
        } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("Table already exists: " + name, e);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Table: " + name, e);
        }
    }
//list methods

public List<Database> getDatabasesForService(String serviceFqn) {
    try {
        DatabasesApi.ListDatabasesQueryParams params = new DatabasesApi.ListDatabasesQueryParams()
                .service(serviceFqn)
                .include("non-deleted")
                .limit(100);

        return apiClient.buildClient(DatabasesApi.class)
                .listDatabases(params)
                .getData();
    } catch (Exception e) {
        throw new OpenMetadataClientException("Failed to get databases for service: " + serviceFqn, e);
    }
}
public List<DatabaseSchema> getSchemasForDatabase(String databaseFqn) {
    try {
        DatabaseSchemasApi schemasApi = apiClient.buildClient(DatabaseSchemasApi.class);

        DatabaseSchemasApi.ListDBSchemasQueryParams queryParams = new DatabaseSchemasApi.ListDBSchemasQueryParams()
            .database(databaseFqn)
            .include("non-deleted") 
            .limit(100); 
        DatabaseSchemaList response = schemasApi.listDBSchemas(queryParams);

        return response.getData();
    } catch (Exception e) {
        throw new OpenMetadataClientException("Failed to fetch schemas for database: " + databaseFqn, e);
    }
}
public List<Table> getTablesForSchema(String schemaFqn) {
    try {
        TablesApi.ListTablesQueryParams params = new TablesApi.ListTablesQueryParams()
                .databaseSchema(schemaFqn)
                .include("non-deleted")
                .limit(100);

        return apiClient.buildClient(TablesApi.class)
                .listTables(params)
                .getData();
    } catch (Exception e) {
        throw new OpenMetadataClientException("Failed to get tables for schema: " + schemaFqn, e);
    }
}

// helper methods

    public Column buildColumn(String name, String description, 
                        String dataTypeStr, String constraintStr) {
        try {
            Column column = new Column();
            column.setName(name);
            column.setDescription(description);
            
            // Convert string to DataTypeEnum
            Column.DataTypeEnum dataType = Column.DataTypeEnum.fromValue(dataTypeStr.toUpperCase());
            column.setDataType(dataType);
            
            // Handle nullable constraint
            if (constraintStr != null && !constraintStr.isEmpty()) {
                Column.ConstraintEnum constraint = Column.ConstraintEnum.fromValue(constraintStr.toUpperCase());
                column.setConstraint(constraint);
            }
            
            return column;
        } catch (IllegalArgumentException e) {
            throw new OpenMetadataClientException(
                "Invalid column definition - " + e.getMessage(), e);
        }
    }

    public EntityReference createEntityReference(User user) {
        return new EntityReference()
                .id(user.getId())
                .type("user")
                .name(user.getName())
                .fullyQualifiedName(user.getFullyQualifiedName())
                .displayName(user.getDisplayName());
    }

    public Map<String, Object> toExtensions(MandatoryFieldsVO fields) {
        return Map.of(
            "Division", List.of(fields.getDivision()),
            "Department", List.of(fields.getDepartment()),
            "DataOrigin", List.of(fields.getDataOrigin()),
            "DataAsset", List.of(fields.getIsDataAsset()),
            "leanIXId", List.of(fields.getLeanIXId()),
            "DocumentationUpdated", List.of(fields.getIsDocumentationUpdated()),
            "DataLakeAvailability", List.of(fields.getIsDataLakeAvailability())
        );
    }

}