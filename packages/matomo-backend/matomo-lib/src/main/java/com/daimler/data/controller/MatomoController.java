package com.daimler.data.controller;

import com.daimler.data.api.matomo.MatomoProjectsApi;
import com.daimler.data.dto.matomo.CreatedByVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Matomo APIs")
@RequestMapping("/api")
@Slf4j
public class MatomoController implements MatomoProjectsApi {
    @Override
    public ResponseEntity<CreatedByVO> getAll(Integer offset, Integer limit) {
        return null;
    }
}
