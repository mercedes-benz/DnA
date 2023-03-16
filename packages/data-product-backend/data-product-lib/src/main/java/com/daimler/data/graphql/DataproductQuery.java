package com.daimler.data.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.daimler.data.controller.DataProductController;
import com.daimler.data.dto.dataproduct.DataProductCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class DataproductQuery implements GraphQLQueryResolver {

    @Autowired
    private DataProductController dataProductController;

    public DataProductCollection getDataproducts(Boolean published, String art,
      String carlafunction, String platform,
      String frontendTool, String productOwner,
      int offset, int limit, String sortBy, String sortOrder) {

        ResponseEntity<DataProductCollection> dataproducts = dataProductController.getAll(
                published, art, carlafunction, platform,
                frontendTool, productOwner, offset, limit,
                sortBy, sortOrder);

        if (dataproducts != null && dataproducts.getBody() != null) {
            return dataproducts.getBody();
        } else {
            return null;
        }
    }
}
