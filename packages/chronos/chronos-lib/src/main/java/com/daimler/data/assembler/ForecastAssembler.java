package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.File;
import com.daimler.data.db.json.Forecast;
import com.daimler.data.db.json.RunDetails;
import com.daimler.data.db.json.RunState;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.dto.forecast.CollaboratorVO;
import com.daimler.data.dto.forecast.CreatedByVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.dto.forecast.InputFileVO;
import com.daimler.data.dto.forecast.RunStateVO;
import com.daimler.data.dto.forecast.RunStateVO.LifeCycleStateEnum;
import com.daimler.data.dto.forecast.RunStateVO.ResultStateEnum;
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
									RunStateVO stateVO = toStateVO(n.getRunState());
									run.setState(stateVO);
									run.setFrequency(toFrequencyEnum(n.getFrequency()));
									return run;
							}).collect(Collectors.toList());
					vo.setRuns(runs);
				}
			}
		}
		return vo;
	}
	
	private RunStateVO toStateVO(RunState runState) {
		RunStateVO stateVO = new RunStateVO();
		if(runState!=null) {
			stateVO.setLifeCycleState(LifeCycleStateEnum.fromValue(runState.getLife_cycle_state()));
			stateVO.setResultState(ResultStateEnum.fromValue(runState.getResult_state()));
			stateVO.setStateMessage(runState.getState_message());
			stateVO.setUserCancelledOrTimedout(runState.getUser_cancelled_or_timedout());
		}
		return stateVO;
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
	
	public List<File> toFiles(List<InputFileVO> filesVO){
		List<File> files =  new ArrayList<>();
		if(filesVO!=null && !filesVO.isEmpty()) {
			files = filesVO.stream().map
					(n -> { File file = new File();
							BeanUtils.copyProperties(n,file);
							return file;
					}).collect(Collectors.toList());
		}
		return files;
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
			if(vo.getSavedInputs()!=null && !vo.getSavedInputs().isEmpty()) {
				List<File> files = this.toFiles(vo.getSavedInputs());
				data.setSavedInputs(files);
			}
			if(vo.getRuns()!=null && !vo.getRuns().isEmpty()) {
				List<RunDetails> runs = vo.getRuns().stream().map
						(n -> { RunDetails run = new RunDetails();
								BeanUtils.copyProperties(n,run);
								RunState state = toState(n.getState());
								run.setRunState(state);
								run.setFrequency(toFrequencyParam(n.getFrequency().name()));
								return run;
						}).collect(Collectors.toList());
				data.setRuns(runs);
			}
			entity.setData(data);
		}
		return entity;
	}
	
	private String toFrequencyParam(String value) {
		switch(value) {
		case "Daily" : return "D";
		case "Weekly" : return "W";
		case "Monthly" : return "M";
		case "Yearly" : return "Y";
		default: return "";
		}
	}
	
	private com.daimler.data.dto.forecast.RunVO.FrequencyEnum toFrequencyEnum(String value) {
		if(value==null)
			value = "";
		switch(value) {
		case "D" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.DAILY;
		case "W" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.WEEKLY;
		case "M" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.MONTHLY;
		case "Y" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.YEARLY;
		default: return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.NO_FREQUENCY;
		}
	}

	private RunState toState(@Valid RunStateVO stateVO) {
		RunState state = new RunState();
		if(stateVO!=null) {
			state.setLife_cycle_state(stateVO.getLifeCycleState().name());
			state.setResult_state(stateVO.getResultState().name());
			state.setState_message(stateVO.getStateMessage());
			state.setUser_cancelled_or_timedout(stateVO.isUserCancelledOrTimedout());
		}
		return state;
	}

}
