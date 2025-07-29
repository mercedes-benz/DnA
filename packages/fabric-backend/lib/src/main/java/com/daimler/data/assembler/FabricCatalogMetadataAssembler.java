package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.beans.BeanUtils;

import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadataDetails;
import com.daimler.data.db.json.catalogManangement.FabricCatalogMetadata;
import com.daimler.data.db.json.catalogManangement.Databases;
import com.daimler.data.db.json.catalogManangement.Schemas;
import com.daimler.data.db.json.catalogManangement.Tables;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataDetailsVO;
import com.daimler.data.dto.fabricCatalogManagement.FabricCatalogMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.DatabaseMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.SchemaMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.TableMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.ColumnMetadataVO;
import com.daimler.data.dto.fabricCatalogManagement.CreatedByVO;

@Component
public class FabricCatalogMetadataAssembler implements GenericAssembler<FabricCatalogMetadataDetailsVO, FabricCatalogMetadataNsql> {

    public FabricCatalogMetadataNsql toEntity(FabricCatalogMetadataDetailsVO vo) {
        FabricCatalogMetadataNsql entity = new FabricCatalogMetadataNsql();
        FabricCatalogMetadataDetails data = new FabricCatalogMetadataDetails();
        FabricCatalogMetadata metadata = new FabricCatalogMetadata();
		if (vo != null) {
            if(vo.getMetadata() != null) {
                FabricCatalogMetadataVO metadataVO = vo.getMetadata();
				metadata.setServiceName(metadataVO.getServiceName());

                List<Databases> dbEntities = new ArrayList<>();
                if (metadataVO.getDatabases() != null) {
                    for (DatabaseMetadataVO dbVo : metadataVO.getDatabases()) {
                        Databases dbEntity = new Databases();
                        dbEntity.setDatabaseName(dbVo.getDbName());

                        List<Schemas> schemaEntities = new ArrayList<>();
                        if (dbVo.getSchemas() != null) {
                            for (SchemaMetadataVO schemaVo : dbVo.getSchemas()) {
                                Schemas schemaEntity = new Schemas();
                                schemaEntity.setSchemaName(schemaVo.getSchemaName());

                                List<Tables> tableEntities = new ArrayList<>();
                                if (schemaVo.getTables() != null) {
                                    for (TableMetadataVO tableVo : schemaVo.getTables()) {
                                        Tables tableEntity = new Tables();
                                        tableEntity.setTableName(tableVo.getTableName());

                                        List<String> columnNames = new ArrayList<>();
                                        if (tableVo.getColumns() != null) {
                                            for (ColumnMetadataVO columnVo : tableVo.getColumns()) {
                                                columnNames.add(columnVo.getColumnName());
                                            }
                                        }
                                        tableEntity.setColumns(columnNames);
                                        tableEntities.add(tableEntity);
                                    }
                                }
                                schemaEntity.setTables(tableEntities);
                                schemaEntities.add(schemaEntity);
                            }
                        }
                        dbEntity.setSchemas(schemaEntities);
                        dbEntities.add(dbEntity);
                    }
                }
                metadata.setDatabases(dbEntities);
                
            }
            data.setMetadata(metadata);
            List<UserDetails> owners = new ArrayList<>();
            if(vo.getOwners() != null) {
                List<CreatedByVO> ownersDetails = vo.getOwners();
                owners = ownersDetails.stream()
                        .map(this::toUserDetails)
                        .collect(Collectors.toList());
            }
            data.setOwners(owners);
        }
        entity.setData(data);
        return entity;
    }

    public FabricCatalogMetadataDetailsVO toVo(FabricCatalogMetadataNsql entity) {
        FabricCatalogMetadataDetailsVO vo = new FabricCatalogMetadataDetailsVO();
        FabricCatalogMetadataVO metadataVO = new FabricCatalogMetadataVO();

		if (entity != null) {
            FabricCatalogMetadataDetails metadataDetails = entity.getData();
            if (metadataDetails != null) {
                    FabricCatalogMetadata metadata = metadataDetails.getMetadata();
                    metadataVO.setServiceName(metadata.getServiceName());

                List<DatabaseMetadataVO> dbVos = new ArrayList<>();
                if (metadata.getDatabases() != null) {
                    for (Databases dbEntity : metadata.getDatabases()) {
                        DatabaseMetadataVO dbVo = new DatabaseMetadataVO();
                        dbVo.setDbName(dbEntity.getDatabaseName());

                        List<SchemaMetadataVO> schemaVos = new ArrayList<>();
                        if (dbEntity.getSchemas() != null) {
                            for (Schemas schemaEntity : dbEntity.getSchemas()) {
                                SchemaMetadataVO schemaVo = new SchemaMetadataVO();
                                schemaVo.setSchemaName(schemaEntity.getSchemaName());

                                List<TableMetadataVO> tableVos = new ArrayList<>();
                                if (schemaEntity.getTables() != null) {
                                    for (Tables tableEntity : schemaEntity.getTables()) {
                                        TableMetadataVO tableVo = new TableMetadataVO();
                                        tableVo.setTableName(tableEntity.getTableName());

                                        List<ColumnMetadataVO> columnVos = new ArrayList<>();
                                        if (tableEntity.getColumns() != null) {
                                            for (String columnName : tableEntity.getColumns()) {
                                                ColumnMetadataVO colVo = new ColumnMetadataVO();
                                                colVo.setColumnName(columnName);
                                                columnVos.add(colVo);
                                            }
                                        }
                                        tableVo.setColumns(columnVos);
                                        tableVos.add(tableVo);
                                    }
                                }
                                schemaVo.setTables(tableVos);
                                schemaVos.add(schemaVo);
                            }
                        }
                        dbVo.setSchemas(schemaVos);
                        dbVos.add(dbVo);
                    }
                }
                metadataVO.setDatabases(dbVos);
            }
            vo.setMetadata(metadataVO);

            List<CreatedByVO> owners = new ArrayList<>();
            if(metadataDetails.getOwners() != null) {
                List<UserDetails> ownerDetails = metadataDetails.getOwners();
                owners = ownerDetails.stream()
                        .map(this::toCreatedByVO)
                        .collect(Collectors.toList());
                
            }
            vo.setOwners(owners);
            
		}
		return vo;
	}

    public UserDetails toUserDetails(CreatedByVO createdBy) {
		UserDetails userDetails = new UserDetails();
		if (createdBy != null) {
			BeanUtils.copyProperties(createdBy, userDetails);
		}
		return userDetails;
	}

	public CreatedByVO toCreatedByVO(UserDetails userDetails) {
		CreatedByVO createdByVO = new CreatedByVO();
		if (userDetails != null) {
			BeanUtils.copyProperties(userDetails, createdByVO);
		}
		return createdByVO;
	}
}
