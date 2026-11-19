package com.daimler.data.util;

import java.util.List;
import java.util.Map;

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

public static boolean validateResultMap(Map<String, String> resultMap) {  
    if (resultMap == null || resultMap.isEmpty()) {  
        return false;  
    }  
  
    List<String> requiredKeys = List.of(  
        "Group",  
        "Permissions",  
        "LakehouseID",  
        "catalogStatus",  
        "mirrorCatalogURL",  
        "createdAt"  
    );  
  
    return requiredKeys.stream()  
        .allMatch(key -> resultMap.containsKey(key) && resultMap.get(key) != null && !resultMap.get(key).isBlank());  
}
    
}
