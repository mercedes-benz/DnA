package com.daimler.data.assembler;

import com.daimler.data.db.entities.AnalyticsSolutionNsql;
import com.daimler.data.db.jsonb.AnalyticsSolution;
import com.daimler.data.dto.analyticsSolution.AnalyticsSolutionVO;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsSolutionAssembler implements GenericAssembler<AnalyticsSolutionVO, AnalyticsSolutionNsql> {

        @Override
        public AnalyticsSolutionVO toVo(AnalyticsSolutionNsql entity) {
            AnalyticsSolutionVO analyticsSolutionVO = null;
            if (entity != null) {
                analyticsSolutionVO = new AnalyticsSolutionVO();
                analyticsSolutionVO.setId(entity.getId());
                analyticsSolutionVO.setName(entity.getData().getName());
            }
            return analyticsSolutionVO;
        }

        @Override
        public AnalyticsSolutionNsql toEntity(AnalyticsSolutionVO vo) {
            AnalyticsSolutionNsql analyticsSolutionNsql = null;
            if (vo != null) {
                analyticsSolutionNsql = new AnalyticsSolutionNsql();
                AnalyticsSolution analyticsSolution = new AnalyticsSolution();
                analyticsSolution.setName(vo.getName());
                analyticsSolutionNsql.setData(analyticsSolution);
                if (vo.getId() != null)
                    analyticsSolutionNsql.setId(vo.getId());
            }
            return analyticsSolutionNsql;
        }
}