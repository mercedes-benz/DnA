package com.daimler.data.application.client;

import com.daimler.data.controller.exceptions.OpenMetadataClientException;
import com.daimler.data.controller.exceptions.EntityNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMetadataClient {

    private final ApiClient apiClient;

   
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
        } catch (Exception e) {
            throw new EntityNotFoundException("DatabaseService", name);
        }
    }

    public DatabaseService createDatabaseService(String name, List<EntityReference> owners ) {
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
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create DatabaseService: " + name, e);
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

    public Database createDatabase(String name, String serviceFQN) {
        try {
            CreateDatabase request = new CreateDatabase()
                    .name(name)
                    .service(serviceFQN);  // Using FQN directly as string
            
            return apiClient.buildClient(DatabasesApi.class)
                    .createOrUpdateDatabase(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Database: " + name, e);
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

    public DatabaseSchema createSchema(String name, String dbFQN) {
        try {
            CreateDatabaseSchema request = new CreateDatabaseSchema()
                    .name(name)
                    .database(dbFQN);  
            
            return apiClient.buildClient(DatabaseSchemasApi.class)
                    .createOrUpdateDBSchema(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Schema: " + name, e);
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

    public Table createTable(String name, String schemaFQN, List<Column> columns) {
        try {
            CreateTable request = new CreateTable()
                    .name(name)
                    .databaseSchema(schemaFQN) 
                    .columns(columns);
            
            return apiClient.buildClient(TablesApi.class)
                    .createOrUpdateTable(request);
        } catch (Exception e) {
            throw new OpenMetadataClientException("Failed to create Table: " + name, e);
        }
    }

   
    public Column buildColumn(String name, String description, 
                            Column.DataTypeEnum dataType, 
                            Column.ConstraintEnum constraint) {
        Column column = new Column();
        column.setName(name);
        column.setDescription(description);
        column.setDataType(dataType);
        if (constraint != null) {
            column.setConstraint(constraint);
        }
        return column;
    }

    public EntityReference createEntityReference(User user) {
    return new EntityReference()
        .id(user.getId())
        .type("user")
        .name(user.getName())
        .fullyQualifiedName(user.getFullyQualifiedName())
        .displayName(user.getDisplayName());
    }
       // ========== Update Methods ========== //

    // public DatabaseService updateDatabaseService(String name, DatabaseService service) {
    //     try {
    //         CreateDatabaseService updateRequest = new CreateDatabaseService()
    //             .name(name)
    //             .serviceType(service.getServiceType())
    //             .connection(service.getConnection())
    //             .owners(service.getOwners())
    //             .description(service.getDescription());
            
    //         return apiClient.buildClient(DatabaseServicesApi.class)
    //             .createDatabaseService(updateRequest);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException("Failed to update DatabaseService: " + name, e);
    //     }
    // }

    // public Database updateDatabase(String name, String serviceFQN, Database database) {
    //     try {
    //         CreateDatabase updateRequest = new CreateDatabase()
    //             .name(name)
    //             .service(serviceFQN)
    //             .description(database.getDescription());
            
    //         return apiClient.buildClient(DatabasesApi.class)
    //             .createOrUpdateDatabase(updateRequest);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to update Database: " + serviceFQN + "." + name, e);
    //     }
    // }

    // public DatabaseSchema updateSchema(String name, String dbFQN, DatabaseSchema schema) {
    //     try {
    //         CreateDatabaseSchema updateRequest = new CreateDatabaseSchema()
    //             .name(name)
    //             .database(dbFQN)
    //             .description(schema.getDescription());
            
    //         return apiClient.buildClient(DatabaseSchemasApi.class)
    //             .createOrUpdateDBSchema(updateRequest);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to update Schema: " + dbFQN + "." + name, e);
    //     }
    // }

    // public Table updateTable(String name, String schemaFQN, Table table) {
    //     try {
    //         CreateTable updateRequest = new CreateTable()
    //             .name(name)
    //             .databaseSchema(schemaFQN)
    //             .columns(table.getColumns())
    //             .description(table.getDescription());
            
    //         return apiClient.buildClient(TablesApi.class)
    //             .createOrUpdateTable(updateRequest);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to update Table: " + schemaFQN + "." + name, e);
    //     }
    // }

    // // ========== Delete Methods ========== //

    // public void deleteDatabaseService(String name) {
    //     try {
    //         DatabaseService service = getDatabaseService(name);
    //         apiClient.buildClient(DatabaseServicesApi.class)
    //             .deleteDatabaseService(service.getId());
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException("Failed to delete DatabaseService: " + name, e);
    //     }
    // }

    // public void deleteDatabase(String serviceName, String dbName) {
    //     try {
    //         Database database = getDatabase(serviceName, dbName);
    //         apiClient.buildClient(DatabasesApi.class)
    //             .deleteDatabase(database.getId());
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to delete Database: " + serviceName + "." + dbName, e);
    //     }
    // }

    // public void deleteSchema(String dbFQN, String schemaName) {
    //     try {
    //         DatabaseSchema schema = getSchema(dbFQN, schemaName);
    //         apiClient.buildClient(DatabaseSchemasApi.class)
    //             .deleteDBSchema(schema.getId());
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to delete Schema: " + dbFQN + "." + schemaName, e);
    //     }
    // }

    // public void deleteTable(String schemaFQN, String tableName) {
    //     try {
    //         Table table = getTable(schemaFQN, tableName);
    //         apiClient.buildClient(TablesApi.class)
    //             .deleteTable(table.getId());
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to delete Table: " + schemaFQN + "." + tableName, e);
    //     }
    // }

    // ========== Patch Methods (Partial Updates) ========== //

    // public DatabaseService patchDatabaseService(UUID serviceId, String patchJson) {
    //     try {
    //         return apiClient.buildClient(DatabaseServicesApi.class)
    //             .patchDatabaseService(serviceId, patchJson);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to patch DatabaseService: " + serviceId, e);
    //     }
    // }

    // public Database patchDatabase(UUID databaseId, String patchJson) {
    //     try {
    //         return apiClient.buildClient(DatabasesApi.class)
    //             .patchDatabase(databaseId, patchJson);
    //     } catch (Exception e) {
    //         throw new OpenMetadataClientException(
    //             "Failed to patch Database: " + databaseId, e);
    //     }
    // }


}