package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.dto.UserInfoVO;
import com.mb.dna.datalakehouse.db.entities.TrinoDataLakeNsql;
import com.mb.dna.datalakehouse.db.jsonb.DataLakeTableCollabDetails;
import com.mb.dna.datalakehouse.db.jsonb.DataLakeTableColumnDetails;
import com.mb.dna.datalakehouse.db.jsonb.DatalakeTable;
import com.mb.dna.datalakehouse.db.jsonb.TrinoDataLakeProject;
import com.mb.dna.datalakehouse.db.jsonb.UserInfo;
import com.mb.dna.datalakehouse.dto.DataLakeTableCollabDetailsVO;
import com.mb.dna.datalakehouse.dto.DataLakeTableColumnDetailsVO;
import com.mb.dna.datalakehouse.dto.DatalakeTableVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;

@Component
public class TrinoDataLakeAssembler  implements GenericAssembler<TrinoDataLakeProjectVO, TrinoDataLakeNsql>{

	
	@Override
	public TrinoDataLakeProjectVO toVo(TrinoDataLakeNsql entity) {
		TrinoDataLakeProjectVO datalakeProjectVO = null;
		if (Objects.nonNull(entity)) {
			datalakeProjectVO = new TrinoDataLakeProjectVO();
			datalakeProjectVO.setId(entity.getId());
			TrinoDataLakeProject projectDetails = entity.getData();
			if(projectDetails!=null) {
				BeanUtils.copyProperties(projectDetails, datalakeProjectVO);
				List<DatalakeTableVO> tablesVO = new ArrayList<>();
				if(projectDetails.getTables()!=null && !projectDetails.getTables().isEmpty()) {
					for(DatalakeTable table: projectDetails.getTables()) {
						DatalakeTableVO tableVO = new DatalakeTableVO();
						BeanUtils.copyProperties(table, tableVO);
						List<DataLakeTableColumnDetailsVO> columnsVO = new ArrayList<>();
						if(table.getColumns()!=null && !table.getColumns().isEmpty()) {
							for(DataLakeTableColumnDetails columnDetails : table.getColumns()) {
								DataLakeTableColumnDetailsVO columnVO = new DataLakeTableColumnDetailsVO();
								BeanUtils.copyProperties(columnDetails, columnVO);
								columnsVO.add(columnVO);
							}
						}
						tableVO.setColumns(columnsVO);
						tablesVO.add(tableVO);
					}
				}
				datalakeProjectVO.setTables(tablesVO);
				List<DataLakeTableCollabDetailsVO> collabsVO = new ArrayList<>();
				if(projectDetails.getCollabs()!=null && !projectDetails.getCollabs().isEmpty()) {
					for(DataLakeTableCollabDetails collabDetails : projectDetails.getCollabs()) {
						DataLakeTableCollabDetailsVO collabVO = new DataLakeTableCollabDetailsVO();
						BeanUtils.copyProperties(collabDetails, collabVO);
						UserInfoVO userinfoVO = new UserInfoVO();
						if(collabDetails.getCollaborator()!=null) {
								BeanUtils.copyProperties(collabDetails.getCollaborator(), userinfoVO);
						}
						collabVO.setCollaborator(userinfoVO);
						collabsVO.add(collabVO);
					}
				}
				datalakeProjectVO.setCollabs(collabsVO);
				
			}
		}
		return datalakeProjectVO;
	}

	@Override
	public TrinoDataLakeNsql toEntity(TrinoDataLakeProjectVO vo) {
		TrinoDataLakeNsql entity = null;
		if(vo!=null) {
			entity = new TrinoDataLakeNsql();
			TrinoDataLakeProject projectDetails = new TrinoDataLakeProject();
			if(vo!=null) {
				BeanUtils.copyProperties(vo,projectDetails);
				List<DatalakeTable> tables = new ArrayList<>();
				if(vo.getTables()!=null && !vo.getTables().isEmpty()) {
					for(DatalakeTableVO tableVO: vo.getTables()) {
						DatalakeTable table = new DatalakeTable();
						BeanUtils.copyProperties(tableVO, table);
						List<DataLakeTableColumnDetails> columns = new ArrayList<>();
						if(tableVO.getColumns()!=null && !tableVO.getColumns().isEmpty()) {
							for(DataLakeTableColumnDetailsVO columnVO : tableVO.getColumns()) {
								DataLakeTableColumnDetails columnDetails = new DataLakeTableColumnDetails();
								BeanUtils.copyProperties(columnVO, columnDetails);
								columns.add(columnDetails);
							}
						}
						table.setColumns(columns);
						tables.add(table);
					}
				}
				projectDetails.setTables(tables);
				List<DataLakeTableCollabDetails> collabs = new ArrayList<>();
				if(vo.getCollabs()!=null && !vo.getCollabs().isEmpty()) {
					for(DataLakeTableCollabDetailsVO collabVO : vo.getCollabs()) {
						DataLakeTableCollabDetails collabDetails = new DataLakeTableCollabDetails();
						BeanUtils.copyProperties(collabVO, collabDetails);
						UserInfo userinfo = new UserInfo();
						if(collabVO.getCollaborator()!=null) {
								BeanUtils.copyProperties(collabVO.getCollaborator(), userinfo);
						}
						collabDetails.setCollaborator(userinfo);
						collabs.add(collabDetails);
					}
				}
				projectDetails.setCollabs(collabs);
			}
			entity.setId(vo.getId());
			entity.setData(projectDetails);
		}
		return entity;
	}
	
	
}
