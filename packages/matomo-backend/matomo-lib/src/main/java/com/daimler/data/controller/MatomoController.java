package com.daimler.data.controller;
import com.daimler.data.api.matomo.MatomoSitesApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.matomo.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Matomo APIs")
@RequestMapping("/api")
@Slf4j
public class MatomoController implements MatomoSitesApi {

    @Override
    public ResponseEntity<MatomoResponseVO> createMatomoSite(MatomoSiteCreateRequestWrapperVO matomoRequestVO) {
        return null;
    }

    @Override
    public ResponseEntity<GenericMessage> deleteById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<MatomoCollectionVO> getAll(Integer offset, Integer limit) {
        return null;
    }

    @Override
    public ResponseEntity<MatomoVO> getById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<MatomoResponseVO> updateById(String id, MatomoSiteUpdateRequestVO matomoUpdateRequestVO) {
        return null;
    }


}
