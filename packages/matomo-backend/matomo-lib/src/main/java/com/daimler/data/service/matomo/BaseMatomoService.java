package com.daimler.data.service.matomo;

import com.daimler.data.application.client.MatomoClient;
import com.daimler.data.assembler.MatomoAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.repo.matomo.MatomoCustomRepository;
import com.daimler.data.dto.MatomoGetSiteResponseDto;
import com.daimler.data.dto.MatomoGetSitesAccessCollectionDto;
import com.daimler.data.dto.MatomoGetSitesAccessDto;
import com.daimler.data.dto.matomo.*;
import com.daimler.data.service.common.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseMatomoService extends BaseCommonService<MatomoVO, MatomoNsql, String> implements MatomoService{

    @Autowired
    private MatomoCustomRepository customRepo;
    @Autowired
    private MatomoAssembler assembler;
    @Autowired
    private MatomoClient matomoClient;
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


    @Override
    public Object[]  getAll( int limit,  int offset, String user) {
        Object[] matomoCollectionWrapper = new Object[2];
        long totalCount= 0L;
        MatomoGetSiteResponseDto getSiteResponse =new MatomoGetSiteResponseDto();
        List<MatomoGetSitesAccessDto> getSiteAccess = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        List<MatomoVO> matomoVO = new ArrayList<>();
        List<MatomoVO> newMatomoList = new ArrayList<>();
        List<MatomoVO> matomoVOResponse = new ArrayList<>();
        MatomoGetSitesAccessCollectionDto response= matomoClient.getSitesAccessFromUser(user);
        if(response!=null && response.getData()!=null){
            getSiteAccess= response.getData();
        }
        List<String> siteIdList = getSiteAccess.stream().map(MatomoGetSitesAccessDto::getSite).collect(Collectors.toList());
        List<MatomoNsql> entities = customRepo.getAll(user, offset, limit);
        if (entities != null && !entities.isEmpty()) {
            matomoVO = entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
            for(MatomoVO matomoRecord: matomoVO) {
            if(siteIdList.contains(matomoRecord.getSiteId())){
                List<CollaboratorVO> collaborators = new ArrayList<CollaboratorVO>();
                getSiteResponse = matomoClient.listParticularMatomoSite(matomoRecord.getSiteId());
                if (getSiteResponse != null && "SUCCESS".equalsIgnoreCase(getSiteResponse.getStatus())) {
                    matomoRecord.setSiteName(getSiteResponse.getName());
                    matomoRecord.setSiteUrl(getSiteResponse.getMain_url());
                }

                map = matomoClient.getUsersAccessFromSite(user, matomoRecord.getSiteId());
                for (Map.Entry<String, Object> entry : map.entrySet()) {

                    String key = entry.getKey();
                    if (key.equalsIgnoreCase(user)) {
                        matomoRecord.setPermission(entry.getValue().toString());
                    } else {
                        CollaboratorVO collaborator = new CollaboratorVO();
                        collaborator.setId(entry.getKey());
                        collaborator.setPermission(entry.getValue().toString());
                        collaborators.add(collaborator);
                    }
                }
                matomoRecord.setCollaborators(collaborators);
                matomoVOResponse.add(matomoRecord);

            }
            }
            totalCount = matomoVOResponse.size();
            int endLimit = offset + limit;
            if (endLimit > matomoVOResponse.size()) {
                endLimit = matomoVOResponse.size();
            }
            newMatomoList = matomoVOResponse.subList(offset, endLimit);
            matomoCollectionWrapper[0] = newMatomoList;
            matomoCollectionWrapper[1] = totalCount;


        }
        return  matomoCollectionWrapper;

    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long getCount(String user) {
        return customRepo.getTotalCount(user);
    }
}
