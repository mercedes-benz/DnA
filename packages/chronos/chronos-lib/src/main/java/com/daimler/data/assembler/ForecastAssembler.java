package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.File;
import com.daimler.data.db.json.Forecast;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.CreatedByVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.RunVO;

@Component
public class ForecastAssembler implements GenericAssembler<ForecastVO, ForecastNsql> {

	@Override
	public ForecastVO toVo(ForecastNsql entity) {
		ForecastVO vo = null;
		if(entity!=null) {
			vo = new ForecastVO();
			vo.setId(entity.getId());
			Forecast data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				if(data.getCollaborators()!=null && !data.getCollaborators().isEmpty()) {
					List<CollaboratorVO> collaborators = data.getCollaborators().stream().map
							(n -> { CollaboratorVO user = new CollaboratorVO();
									BeanUtils.copyProperties(n,user);
									return user;
							}).collect(Collectors.toList());
					vo.setCollaborators(collaborators);
					}
				if(data.getCreatedBy()!=null) {
					CreatedByVO creator = new CreatedByVO();
					BeanUtils.copyProperties(data.getCreatedBy(),creator);
					vo.setCreatedBy(creator);
				}
				if(data.getSavedInputs()!=null && !data.getSavedInputs().isEmpty()) {
					List<InputFileVO> files = this.toFilesVO(data.getSavedInputs());
					vo.setSavedInputs(files);
				}
				if(data.getRuns()!=null && !data.getRuns().isEmpty()) {
					List<RunVO> runs = data.getRuns().stream().map
							(n -> { RunVO run = new RunVO();
									BeanUtils.copyProperties(n,run);
									return run;
							}).collect(Collectors.toList());
					vo.setRuns(runs);
				}
			}
		}
		return vo;
	}
	
	public List<InputFileVO> toFilesVO(List<File> files){
		List<InputFileVO> filesVO =  new ArrayList<>();
		if(files!=null && !files.isEmpty()) {
			filesVO = files.stream().map
					(n -> { InputFileVO file = new InputFileVO();
							BeanUtils.copyProperties(n,file);
							return file;
					}).collect(Collectors.toList());
		}
		return filesVO;
	}

	@Override
	public ForecastNsql toEntity(ForecastVO vo) {
		ForecastNsql entity = null;
		if(vo!=null) {
			entity = new ForecastNsql();
			entity.setId(vo.getId());
			Forecast data = new Forecast();
			BeanUtils.copyProperties(vo, data);
			if(vo.getCollaborators()!=null && !vo.getCollaborators().isEmpty()) {
				List<UserDetails> collaborators = vo.getCollaborators().stream().map
								(n -> { UserDetails collaborator = new UserDetails();
								BeanUtils.copyProperties(n,collaborator);
								return collaborator;
						}).collect(Collectors.toList());
				data.setCollaborators(collaborators);
			}
			if(vo.getCreatedBy()!=null) {
				UserDetails creator = new UserDetails();
				BeanUtils.copyProperties(vo.getCreatedBy(), creator);
				data.setCreatedBy(creator);
			}
		}
		return entity;
	}

}
