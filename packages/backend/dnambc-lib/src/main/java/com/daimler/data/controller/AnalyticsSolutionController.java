package com.daimler.data.controller;

import com.daimler.data.api.analyticsSolution.AnalyticsSolutionsApi;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionCollection;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionRequestVO;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Analytics Solution API", tags = { "analyticsSolutions" })
@RequestMapping("/api")
@Slf4j
public class AnalyticsSolutionController implements AnalyticsSolutionsApi {


    @Override
    public ResponseEntity<AnalyticsSolutionVO> createAnalyticsSolution(AnalyticsSolutionRequestVO analyticsSolutionRequestVO) {
        return null;
    }

    @Override
    public ResponseEntity<AnalyticsSolutionCollection> getAll(String sortOrder) {
        return null;
    }
}
