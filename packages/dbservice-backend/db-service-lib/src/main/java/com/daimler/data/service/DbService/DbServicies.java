package com.daimler.data.service.DbService;

import java.util.List;

import com.daimler.data.db.entities.DbServiceNsql;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.dto.dbService.CredentialsVO;
import com.daimler.data.dto.dbService.DbServiceVO;
import com.daimler.data.dto.dbService.GenericMessage;
import com.daimler.data.dto.dbService.InitializeResponseVo;
import com.daimler.data.dto.dbService.UserInfoVO;
import com.daimler.data.service.common.CommonService;

public interface DbServicies extends CommonService<DbServiceVO, DbServiceNsql, String> {

    InitializeResponseVo createDb(DbServiceVO serviceVo);

    List<CredentialsVO> getCredentials(UserInfoVO user,String serviceName);

    List<DbServiceVO> getAllDbService(int offset, int limit,String id);

    InitializeResponseVo editDb(DbServiceVO serviceVo,UserInfoVO user);

    GenericMessage deleteDb(DbServiceVO serviceVo);

}
