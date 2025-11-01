package com.daimler.data.service.uiliciousWorkspace;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;

import com.daimler.data.service.common.CommonService;
import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.db.jsonb.UiliciousWorkspace;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateRequestVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceUpdateResponseVO;
import com.daimler.data.dto.uilicious.CreateUiliciousWorkspaceRequestVO;
import com.daimler.data.dto.UiliciousCreationDTO;

public interface UiliciousWorkspaceService extends CommonService<UiliciousCreationDTO, UiliciousWorkspaceNsql, String> {
      UiliciousWorkspacesCollectionVO getUiliciousWorkspaces(Integer offset, Integer limit, String sortOrder);
      
      UiliciousWorkspaceUpdateResponseVO updateUiliciousWorkspace(UiliciousWorkspaceUpdateRequestVO request);

      String createUiliciousWorkspace(CreateUiliciousWorkspaceRequestVO request);

}
	


