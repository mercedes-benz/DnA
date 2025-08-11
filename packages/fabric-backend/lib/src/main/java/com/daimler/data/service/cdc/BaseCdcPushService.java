package com.daimler.data.service.cdc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.application.client.CdcPushClient;
import com.daimler.data.dto.cdc.LakehouseTablesCollectionDto;
import com.daimler.data.dto.cdc.LakehouseTablesDto;
import com.daimler.data.dto.cdc.TableSchemaCollectionDto;
import com.daimler.data.dto.cdc.TableSchemaDto;
import com.daimler.data.dto.cdcPush.LakehouseTableCollectionVO;
import com.daimler.data.dto.cdcPush.LakehouseTableVO;
import com.daimler.data.dto.cdcPush.TableColumnVO;
import com.daimler.data.dto.cdcPush.TableSchemaDataVO;
import com.daimler.data.dto.cdcPush.TableSchemaResponseVO;
import com.daimler.data.service.cdc.CdcPushService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseCdcPushService implements CdcPushService{

    @Autowired
    private CdcPushClient cdcPushClient;

    @Override
    public LakehouseTableCollectionVO getLakehouseTables(String workspaceId, String lakehouseId) {
        LakehouseTableCollectionVO collectionVO = new LakehouseTableCollectionVO();
        LakehouseTablesCollectionDto collection = cdcPushClient.getLakehouseTables(workspaceId, lakehouseId);

        if (collection != null && collection.getData() != null && collection.getData().getTables() != null
                && !collection.getData().getTables().isEmpty()) {

            List<LakehouseTablesDto> tableDtos = collection.getData().getTables();

            List<LakehouseTableVO> records = tableDtos.stream()
                    .map(dto -> new LakehouseTableVO()
                            .name(dto.getTableName())
                            .schema(dto.getSchema()))
                    .collect(Collectors.toList());

            collectionVO.records(records);
        } else {
            collectionVO.records(new ArrayList<>());
        }
        return collectionVO;
    }

    @Override
    public TableSchemaResponseVO getTableSchema(String workspaceId, String lakehouseId, String tableName, String schemaName) {
        TableSchemaResponseVO responseVO = new TableSchemaResponseVO();
        TableSchemaDataVO dataVO = new TableSchemaDataVO();

        TableSchemaCollectionDto collectionDto = cdcPushClient.getTableSchema(workspaceId, lakehouseId, tableName, schemaName);

        if (collectionDto != null && collectionDto.getData() != null && collectionDto.getData().getColumns() != null
                && !collectionDto.getData().getColumns().isEmpty()) {

            List<TableSchemaDto> schemaDtos = collectionDto.getData().getColumns();

            List<TableColumnVO> columns = schemaDtos.stream()
                    .map(dto -> {
                        TableColumnVO vo = new TableColumnVO();
                        vo.setColumnName(dto.getColumnName());
                        vo.setColType(dto.getColType());
                        vo.setColConstraint(dto.getColConstraint());
                        return vo;
                    })
                    .collect(Collectors.toList());

            dataVO.setColumns(columns);
        } else {
            dataVO.setColumns(new ArrayList<>());
        }

        responseVO.setData(dataVO);
        return responseVO;
    }
    
}
