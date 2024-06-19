package com.daimler.data.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.daimler.data.controller.DataProductController;
import com.daimler.data.dto.dataproduct.DataProductCollection;
import org.springframework.http.ResponseEntity;


@Controller
public class DataproductQuery{

    @Autowired
    private DataProductController dataProductController;

    @QueryMapping
    public DataProductCollection dataproducts(@Argument Boolean published,@Argument String art,
    @Argument String carlafunction,@Argument String platform,
    @Argument String frontendTool, @Argument String productOwner,
    @Argument int offset,@Argument  int limit,@Argument String sortBy, @Argument String sortOrder,@Argument String dataSteward,@Argument String informationOwner,@Argument String department,@Argument String division) {
        ResponseEntity<DataProductCollection> dataproducts = dataProductController.getAll(
                published, art, carlafunction, platform,
                frontendTool, productOwner, offset, limit,
                sortBy, sortOrder,dataSteward,informationOwner,department,division);
        if (dataproducts != null && dataproducts.getBody() != null) {
            return dataproducts.getBody();
        } else {
            return new DataProductCollection();
        }
    }
}
