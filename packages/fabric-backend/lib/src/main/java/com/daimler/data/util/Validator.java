package com.daimler.data.util;

import com.daimler.data.dto.fabricCatalogManagement.CreateMirroredCatalogRequestVO;

public class Validator {


    public static String getMissingField(CreateMirroredCatalogRequestVO createMirroredCatalogRequest) {  
    if (createMirroredCatalogRequest.getDataProductName() == null || createMirroredCatalogRequest.getDataProductName().isBlank()) {  
        return "dataProductName";  
    }  
    if (createMirroredCatalogRequest.getCatalogName() == null || createMirroredCatalogRequest.getCatalogName().isBlank()) {  
        return "catalogName";  
    }  
    if (createMirroredCatalogRequest.getSchemaName() == null || createMirroredCatalogRequest.getSchemaName().isBlank()) {  
        return "schemaName";  
    }  
    if (createMirroredCatalogRequest.getRegion() == null || createMirroredCatalogRequest.getRegion().isBlank()) {  
        return "region";  
    }  
    if (createMirroredCatalogRequest.getStorageAccountUrl() == null || createMirroredCatalogRequest.getStorageAccountUrl().isBlank()) {  
        return "storageAccountUrl";  
    }  
    if (createMirroredCatalogRequest.getDdxGroup() == null || createMirroredCatalogRequest.getDdxGroup().isBlank()) {  
        return "ddxGroup";  
    }  
    if (createMirroredCatalogRequest.getDdxCorrelationId() == null || createMirroredCatalogRequest.getDdxCorrelationId().isBlank()) {  
        return "ddxCorrelationId";  
    }  
    return null;  
}
    
}
