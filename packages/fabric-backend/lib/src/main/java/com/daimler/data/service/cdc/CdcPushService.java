package com.daimler.data.service.cdc;

import com.daimler.data.service.common.CommonService;

import com.daimler.data.dto.cdcPush.LakehouseTableCollectionVO;
import com.daimler.data.dto.cdcPush.TableSchemaResponseVO;

public interface CdcPushService {
    LakehouseTableCollectionVO getLakehouseTables(String workspaceId, String lakehouseId);

    TableSchemaResponseVO getTableSchema(String workspaceId, String lakehouseId, String tableName, String schemaName);
}
