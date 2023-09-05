package com.daimler.data.service.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.dto.matomo.MatomoVO;
import com.daimler.data.service.common.CommonService;

public interface MatomoService extends CommonService<MatomoVO, MatomoNsql, String> {
}
