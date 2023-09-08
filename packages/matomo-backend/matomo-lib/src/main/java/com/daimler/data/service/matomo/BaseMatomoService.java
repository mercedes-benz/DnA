package com.daimler.data.service.matomo;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.dto.matomo.*;
import com.daimler.data.service.common.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BaseMatomoService extends BaseCommonService<MatomoVO, MatomoNsql, String> implements MatomoService{

    @Override
    public MatomoResponseVO createMatomoSite(String matomoId, String siteId, Date createdOn, Date lastModified, MatomoSiteRequestVO matomoRequestVO, CreatedByVO requestUser) {
        GenericMessage responseMessage = new GenericMessage();
        MatomoVO matomoVO = new MatomoVO();
        MatomoSiteVO matomoSiteVO =new MatomoSiteVO();
        matomoVO.setId(matomoId);
        matomoVO.setSiteId(siteId);
        matomoVO.setSiteName(matomoRequestVO.getSiteName());
        matomoVO.setSiteUrl(matomoRequestVO.getSiteUrl());
        matomoVO.setDivision(matomoRequestVO.getDivision());
        matomoVO.setSubDivision(matomoRequestVO.getSubDivision());
        matomoVO.setDepartment(matomoRequestVO.getDepartment());
        matomoVO.setClassificationType(matomoRequestVO.getClassificationType());
        matomoVO.setPiiData(matomoRequestVO.isPiiData());
        matomoVO.setCreatedBy(requestUser);
        matomoVO.setStatus(matomoRequestVO.getStatus());
        matomoVO.setPermission(matomoRequestVO.getPermission());
        matomoVO.setCollaborators(matomoRequestVO.getCollaborators());
        matomoVO.setCreatedOn(createdOn);
        matomoVO.setLastModified(lastModified);

        MatomoResponseVO responseWrapperVO = new MatomoResponseVO();
        try {
            super.create(matomoVO);
            responseMessage.setSuccess("SUCCESS");

        }catch(Exception e) {
            log.error("Failed while saving details of matomo site {} to database for site {}, created by {}",siteId, matomoRequestVO.getSiteName(), requestUser);
            MessageDescription msg = new MessageDescription("Failed to save matomo site details to table ");
            List<MessageDescription> errors = new ArrayList<>();
            errors.add(msg);
            responseMessage.setErrors(errors);
        }
        responseWrapperVO.setData(matomoVO);
        responseWrapperVO.setResponse(responseMessage);
        return responseWrapperVO;
    }
}
