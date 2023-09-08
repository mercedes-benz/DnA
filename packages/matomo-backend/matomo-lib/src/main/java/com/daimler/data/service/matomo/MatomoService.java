package com.daimler.data.service.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.dto.matomo.*;
import com.daimler.data.service.common.CommonService;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

public interface MatomoService extends CommonService<MatomoVO, MatomoNsql, String> {
    MatomoResponseVO createMatomoSite(String matomoId, String siteId, Date createdOn, Date lastModified, MatomoSiteRequestVO matomoRequestVO, CreatedByVO requestUser);
}
