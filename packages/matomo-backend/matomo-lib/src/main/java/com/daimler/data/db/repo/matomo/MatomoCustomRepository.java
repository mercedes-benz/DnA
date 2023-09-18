package com.daimler.data.db.repo.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

import java.util.List;

public interface MatomoCustomRepository extends CommonDataRepository<MatomoNsql, String>{

    MatomoNsql findUserById(String userId);

}
