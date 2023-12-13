package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.mb.dna.datalakehouse.db.entities.TrinoAccessNsql;
import com.mb.dna.datalakehouse.db.jsonb.TrinoAccess;
import com.mb.dna.datalakehouse.db.jsonb.TrinoCatalogRules;
import com.mb.dna.datalakehouse.db.jsonb.TrinoSchemaRules;
import com.mb.dna.datalakehouse.db.jsonb.TrinoTableRules;
import com.mb.dna.datalakehouse.dto.TrinoAccessVO;
import com.mb.dna.datalakehouse.dto.TrinoCatalogRulesVO;
import com.mb.dna.datalakehouse.dto.TrinoSchemaRulesVO;
import com.mb.dna.datalakehouse.dto.TrinoTableRulesVO;

@Component
public class TrinoAccessAssembler  implements GenericAssembler<TrinoAccessVO, TrinoAccessNsql>{

	
	@Override
	public TrinoAccessVO toVo(TrinoAccessNsql entity) {
		TrinoAccessVO accessVO = null;
		if (Objects.nonNull(entity)) {
			accessVO = new TrinoAccessVO();
			TrinoAccess accessDetails = entity.getData();
			if(accessDetails!=null) {
				List<TrinoCatalogRulesVO> catalogs = new ArrayList<>();
				List<TrinoSchemaRulesVO> schemas = new ArrayList<>();
				List<TrinoTableRulesVO> tables = new ArrayList<>();
				if(accessDetails.getCatalogs()!=null && !accessDetails.getCatalogs().isEmpty()) {
					for(TrinoCatalogRules catalogRule : accessDetails.getCatalogs()) {
						TrinoCatalogRulesVO catalogRuleVO = new TrinoCatalogRulesVO();
						BeanUtils.copyProperties(catalogRule, catalogRuleVO);
						catalogs.add(catalogRuleVO);
					}
				}
				if(accessDetails.getSchemas()!=null && !accessDetails.getSchemas().isEmpty()) {
					for(TrinoSchemaRules schemaRule : accessDetails.getSchemas()) {
						TrinoSchemaRulesVO schemaRuleVO = new TrinoSchemaRulesVO();
						BeanUtils.copyProperties(schemaRule, schemaRuleVO);
						schemas.add(schemaRuleVO);
					}
				}
				if(accessDetails.getTables()!=null && !accessDetails.getTables().isEmpty()) {
					for(TrinoTableRules tableRule : accessDetails.getTables()) {
						TrinoTableRulesVO tableRuleVO = new TrinoTableRulesVO();
						BeanUtils.copyProperties(tableRule, tableRuleVO);
						tables.add(tableRuleVO);
					}
				}
				accessVO.setCatalogs(catalogs);
				accessVO.setSchemas(schemas);
				accessVO.setTables(tables);
			}
		}
		return accessVO;
	}

	@Override
	public TrinoAccessNsql toEntity(TrinoAccessVO vo) {
		TrinoAccessNsql entity = null;
		if(vo!=null) {
			entity = new TrinoAccessNsql();
			TrinoAccess accessDetails = new TrinoAccess();
			BeanUtils.copyProperties(vo,accessDetails);
			entity.setData(accessDetails);
		}
		return entity;
	}
	
	
}
