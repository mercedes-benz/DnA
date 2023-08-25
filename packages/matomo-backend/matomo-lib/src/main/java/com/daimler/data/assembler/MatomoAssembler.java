package com.daimler.data.assembler;

import com.daimler.data.db.entities.MatomoNsql;
import com.daimler.data.dto.matomo.CreatedByVO;
import org.springframework.stereotype.Component;

@Component
public class MatomoAssembler implements GenericAssembler<CreatedByVO, MatomoNsql> {

    @Override
    public CreatedByVO toVo(MatomoNsql matomoNsql) {
        return null;
    }

    @Override
    public MatomoNsql toEntity(CreatedByVO vo) {
        return null;
    }
}
