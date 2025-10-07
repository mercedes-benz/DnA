package com.daimler.data.service.uiliciousWorkspace;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import com.daimler.data.dto.uilicious.UiliciousWorkspacesCollectionVO;
import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.uiliciousWorkspace.UiliciousWorkspaceService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.client.uiLicious.UiLiciousClient;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
//import com.daimler.data.application.auth.UserInfo;


@Service
@Slf4j
public class BaseUiliciousWorkspaceService extends BaseCommonService<UiliciousWorkspaceVO, UiliciousWorkspaceNsql, String> implements UiliciousWorkspaceService{
	@Override
	public UiliciousWorkspacesCollectionVO getUiliciousWorkspaces(String userId, Integer page, Integer size, String filter) {
		//System.out.println("getUiliciousWorkspaces called with userId: " + userId + ", page: " + page + ", size: " + size + ", filter: " + filter);
		return null;
	}
}



