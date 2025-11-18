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
import org.openmetadata.schema.metadataIngestion.FilterPattern;
import org.openmetadata.schema.services.connections.database.DatalakeConnection;
import org.openmetadata.schema.services.connections.database.SampleDataStorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import feign.FeignException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMetadataClient {

    private final ApiClient apiClient;

    @Value("${cdcIntegration.openmetadata.tags}")
    String[] defaultTags;

// getbyFqn methods

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
//get by id methods

    public DatabaseSchema getSchemaById(String schemaId) {
        try {
            return apiClient.buildClient(DatabaseSchemasApi.class)
                .getDBSchemaByID(UUID.fromString(schemaId), "database", null);
        } catch (Exception e) {
            throw new EntityNotFoundException("Schema", schemaId);
        }
    }

    public Database getDatabaseById(String databaseId) {
        try {
            return apiClient.buildClient(DatabasesApi.class)
                .getDatabaseByID(UUID.fromString(databaseId), "service", null);
        } catch (Exception e) {
            throw new EntityNotFoundException("Database", databaseId);
        }
    }
// Create methods

    public DatabaseService createDatabaseService(String name, List<EntityReference> owners, String description, Integer tier) {
        try {
            CreateDatabaseService request = new CreateDatabaseService()
                    .name(name)
                    .description(description)
                    .tags(getDefaultTags())
                    .addTagsItem(prepareTag(mapTierValue(tier)))
                    .serviceType(CreateDatabaseService.ServiceTypeEnum.DATALAKE);

            Map<String, Object> storageConfig = new HashMap<>();
            Map<String, Object> config = new HashMap<>();
            config.put("bucketName", "");
            config.put("prefix", "");
            config.put("overwriteData", true);
            config.put("storageConfig", storageConfig);
            config.put("filePathPattern", "{service_name}/{database_name}/{database_schema_name}/{table_name}");

            SampleDataStorageConfig sampleDataConfig = new SampleDataStorageConfig();
            sampleDataConfig.setConfig(config);

            DatabaseConnection connection = new DatabaseConnection();
            connection.setConfig(new DatalakeConnection()
                    .withSchemaFilterPattern(new FilterPattern())
                    .withTableFilterPattern(new FilterPattern())
                    .withDatabaseFilterPattern(new FilterPattern())
                    .withSampleDataStorageConfig(sampleDataConfig)
                    .withSupportsMetadataExtraction(true)
                    .withBucketName(name)
                    .withDatabaseName("Lakehouses")
                    .withConfigSource(new Object())
                    .withPrefix("DNA-Fabric"));
            request.setConnection(connection);
            request.setOwners(owners);

            return apiClient.buildClient(DatabaseServicesApi.class)
                    .createDatabaseService(request);
        } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("DatabaseService already Exists "+ name, e);
        } catch (FeignException.BadRequest e){
            throw new OpenMetadataClientException("Failed to create DatabaseService : " + name + " Bad Request "+ e.getMessage(), e);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create DatabaseService : " + name, e);
        }
    }

    public Database createDatabase(String name, String serviceFQN, MandatoryFieldsVO fields, List<EntityReference> owners, String description) {
        try {
            CreateDatabase request = new CreateDatabase()
                    .name(name)
                    .service(serviceFQN)
                    .extension(toExtensions(fields))
                    .description(description)
                    .tags(getDefaultTags())
                    .addTagsItem(prepareTag(mapTierValue(fields.getTier())))
                    .owners(owners); // Using FQN directly as string
            return apiClient.buildClient(DatabasesApi.class)
                    .createOrUpdateDatabase(request);
        } catch (FeignException.Conflict e) {
            throw new EntityAlreadyExistsException("Database already exists: " + name, e);
        } catch (FeignException.BadRequest e){
            throw new OpenMetadataClientException("Failed to create Database : " + name + " Bad Request "+ e.getMessage(), e);
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
        } catch (FeignException.BadRequest e){
            throw new OpenMetadataClientException("Failed to create Schema : " + name + " Bad Request "+ e.getMessage(), e);
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
        } catch (FeignException.BadRequest e){
            throw new OpenMetadataClientException("Failed to create Table : " + name + " Bad Request "+ e.getMessage(), e);
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

//update methods
   public DatabaseService updateDatabaseService(CreateDatabaseService updateRequest) {
        try {
            return apiClient.buildClient(DatabaseServicesApi.class)
                .createOrUpdateDatabaseService(updateRequest);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to update database service", e);
        }
    }

    public Database updateDatabase(String dbId, String name, String serviceFQN, MandatoryFieldsVO fields, List<EntityReference> owners) {
        try {
            CreateDatabase request = new CreateDatabase()
                .name(name)
                .service(serviceFQN)
                .extension(toExtensions(fields))
                .owners(owners);

            return apiClient.buildClient(DatabasesApi.class)
                .createOrUpdateDatabase(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to update database: " + name, e);
        }
    }

    public DatabaseSchema updateSchema(String schemaId, String name, String dbFQN) {
        try {
            CreateDatabaseSchema request = new CreateDatabaseSchema()
                .name(name)
                .database(dbFQN);

            return apiClient.buildClient(DatabaseSchemasApi.class)
                .createOrUpdateDBSchema(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to update schema: " + name, e);
        }
    }

    public Table updateTable(String tableId, String name, String schemaFQN, List<Column> columns) {
        try {
            CreateTable request = new CreateTable()
                .name(name)
                .databaseSchema(schemaFQN)
                .columns(columns);

            return apiClient.buildClient(TablesApi.class)
                .createOrUpdateTable(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to update table: " + name, e);
        }
    }

    // For column operations
    public void addColumnToTable(String tableId, Column column) {
        try {
            Table table = getTableById(tableId);
            List<Column> columns = new ArrayList<>(table.getColumns());
            columns.add(column);
            
            CreateTable request = new CreateTable()
                .name(table.getName())
                .databaseSchema(table.getDatabaseSchema().getFullyQualifiedName())
                .columns(columns);

            apiClient.buildClient(TablesApi.class)
                .createOrUpdateTable(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to add column to table: " + tableId, e);
        }
    }

    public void removeColumnFromTable(String tableId, String columnName) {
        try {
            Table table = getTableById(tableId);
            List<Column> columns = table.getColumns().stream()
                .filter(col -> !col.getName().equals(columnName))
                .collect(Collectors.toList());
                
            CreateTable request = new CreateTable()
                .name(table.getName())
                .databaseSchema(table.getDatabaseSchema().getFullyQualifiedName())
                .columns(columns);

            apiClient.buildClient(TablesApi.class)
                .createOrUpdateTable(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to remove column from table: " + tableId, e);
        }
    }

    private Table getTableById(String tableId) {
        try {
            return apiClient.buildClient(TablesApi.class)
                .getTableByID(UUID.fromString(tableId), null, "non-deleted");
        } catch (Exception e) {
            throw new EntityNotFoundException("Table", tableId);
        }
    }

//delete methods

    public void deleteDatabaseService(String fqn) {
        try {
            apiClient.buildClient(DatabaseServicesApi.class)
                .deleteDatabaseServiceByName(
                    fqn,
                    true,  
                    true   
                );
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to delete database service: " + fqn, e);
        }
    }

    public void deleteDatabase(String databaseId) {
        try {
            apiClient.buildClient(DatabasesApi.class)
                .deleteDatabase(
                    UUID.fromString(databaseId),
                    true,  // recursive = true to delete children
                    true  // hardDelete = false (soft delete)
                );
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to delete database: " + databaseId, e);
        }
    }

    public void deleteSchema(String schemaId) {
        try {
            apiClient.buildClient(DatabaseSchemasApi.class)
                .deleteDBSchema(
                    UUID.fromString(schemaId),
                    true,  // recursive = true
                    true  // hardDelete = false
                );
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to delete schema: " + schemaId, e);
        }
    }

    public void deleteTable(String tableId) {
        try {
            apiClient.buildClient(TablesApi.class)
                .deleteTable(
                    UUID.fromString(tableId),
                    true, // hardDelete = false
                    true   // recursive = true
                );
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to delete table: " + tableId, e);
        }
    }

    public void deleteColumnFromTable(String tableId, String columnName) {
        try {
            Table table = getTableById(tableId);
            List<Column> updatedColumns = table.getColumns().stream()
                .filter(col -> !col.getName().equals(columnName))
                .collect(Collectors.toList());
            
            updateTable(tableId, table.getName(), table.getDatabaseSchema().getFullyQualifiedName(), updatedColumns);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to delete column: " + columnName, e);
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
            "Division", fields.getDivisions(),
            "Department", List.of(fields.getDepartment()),
            // "DataOrigin", List.of(fields.getDataOrigin()),
//"IsDataAsset", List.of(fields.getIsDataAsset()),
            "LeanIXID", List.of(fields.getLeanIXId()),
            "DocumentationUpdated", List.of(fields.getIsDocumentationUpdated()),
            "DataLakeAvailability", List.of(fields.getIsDataLakeAvailability()),
            "DataConfidentiality",List.of(fields.getDataConfidentiality())
        );
    }

    public TagLabel prepareTag(String tagFQN){

        TagLabel tag = new TagLabel().tagFQN(tagFQN)
            .labelType(TagLabel.LabelTypeEnum.fromValue("Manual"))
            .state(TagLabel.StateEnum.fromValue("Confirmed"))
            .source(TagLabel.SourceEnum.fromValue("Classification"));

        return tag;
    }

    public String mapTierValue(Integer value) {
        switch(value) {
            case 1:
                return "Tier.Tier1";
            case 2:
                return "Tier.Tier2";
            default:
                throw new IllegalArgumentException("Invalid tier value: " + value);
        }
    }

    public List<TagLabel> getDefaultTags() {
        return Arrays.stream(defaultTags)
                .map(this::prepareTag)
                .collect(Collectors.toList());
    }

}
