package com.daimler.data.service.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.dto.matomo.MatomoVO;
import com.daimler.data.service.common.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaseMatomoService extends BaseCommonService<MatomoVO, MatomoNsql, String> implements MatomoService{

}
