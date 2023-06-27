package com.daimler.data.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;


import com.daimler.data.db.json.*;
import com.daimler.data.dto.forecast.*;
import com.daimler.data.dto.storage.BucketObjectDetailsDto;
import com.daimler.data.dto.storage.BucketObjectsCollectionWrapperDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.dto.forecast.RunStateVO.LifeCycleStateEnum;
import com.daimler.data.dto.forecast.RunStateVO.ResultStateEnum;

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
					List<RunVO> runs = toRunsVO(data.getRuns());
					vo.setRuns(runs);
				}
				if(data.getComparisons()!=null && !data.getComparisons().isEmpty()) {
					List<ForecastComparisonVO> comparisons = toComparisonsVO(data.getComparisons());
					vo.setComparisons(comparisons);
				}
				if(data.getConfigFiles()!=null && !data.getConfigFiles().isEmpty()) {
					List<InputFileVO> configFiles = toConfigFilesVO(data.getConfigFiles());
					vo.setConfigFiles(configFiles);
				}
				vo.setBucketId(entity.getData().getBucketId());
			}
		}
		return vo;
	}
	
	public List<RunVO> toRunsVO(List<RunDetails> runs){
		List<RunVO> runsVOList = new ArrayList<>();
		if(runs!=null && !runs.isEmpty()) {
			for(RunDetails run: runs) {
				if(run.getIsDelete()==null || !run.getIsDelete()) {
					RunVO runVO = new RunVO();
					BeanUtils.copyProperties(run,runVO);
					RunStateVO stateVO = toStateVO(run.getRunState());
					runVO.setState(stateVO);
					if(run.getIsDelete()!=null) 
						runVO.setIsDeleted(run.getIsDelete());
					runVO.setFrequency(toFrequencyEnum(run.getFrequency()));
					runsVOList.add(runVO);
				}
			}
		}
		return runsVOList;
	}

	public List<ForecastComparisonVO> toComparisonsVO(List<ComparisonDetails> comparisons){
		List<ForecastComparisonVO> comparisonsVOList = new ArrayList<>();
		if(comparisons!=null && !comparisons.isEmpty()) {
			for(ComparisonDetails comparison: comparisons) {
				if(comparison.getIsDelete()==null || !comparison.getIsDelete()) {
					ForecastComparisonVO comparisonVO = new ForecastComparisonVO();
					BeanUtils.copyProperties(comparison,comparisonVO);
					ComparisonStateVO comparisonStateVO = toComparisonStateVO(comparison.getComparisonState());
					comparisonVO.setState(comparisonStateVO);
					if(comparison.getIsDelete()!=null)
						comparisonVO.setIsDeleted(comparison.getIsDelete());
					comparisonsVOList.add(comparisonVO);
				}
			}
		}
		return comparisonsVOList;
	}

	public List<InputFileVO> toConfigFilesVO(List<File> configFiles){
		List<InputFileVO> configFilesVO =  new ArrayList<>();
		if(configFiles!=null && !configFiles.isEmpty()) {
			configFilesVO = configFiles.stream().map
					(n -> { InputFileVO file = new InputFileVO();
						BeanUtils.copyProperties(n,file);
						return file;
					}).collect(Collectors.toList());
		}
		return configFilesVO;
	}
	public List<BucketObjectDetailsDto> toProjectSpecificConfigFiles(List<InputFileVO> configFiles){
		List<BucketObjectDetailsDto> filePathList = new ArrayList<>();
		if(configFiles!=null && !configFiles.isEmpty()) {
			 filePathList = configFiles.stream().map(x-> new BucketObjectDetailsDto(x.getPath())).collect(Collectors.toList());
		}
		return filePathList;
	}


	private RunStateVO toStateVO(RunState runState) {
		RunStateVO stateVO = new RunStateVO();
		if(runState!=null) {
			if(runState.getLife_cycle_state()!=null)
				stateVO.setLifeCycleState(LifeCycleStateEnum.fromValue(runState.getLife_cycle_state()));
			if(runState.getResult_state()!=null)
				stateVO.setResultState(ResultStateEnum.fromValue(runState.getResult_state()));
			stateVO.setStateMessage(runState.getState_message());
			stateVO.setUserCancelledOrTimedout(runState.getUser_cancelled_or_timedout());
		}
		return stateVO;
	}

	private ComparisonStateVO toComparisonStateVO(ComparisonState comparisonState) {
		ComparisonStateVO comparisonStateVO = new ComparisonStateVO();
		if(comparisonState!=null) {
			if(comparisonState.getLifeCycleState()!=null)
				comparisonStateVO.setLifeCycleState(ComparisonStateVO.LifeCycleStateEnum.fromValue(comparisonState.getLifeCycleState()));

			comparisonStateVO.setStateMessage(comparisonState.getStateMessage());

		}
		return comparisonStateVO;
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

	public List<File> toConfigFiles(List<InputFileVO> configFilesVO){
		List<File> configFiles =  new ArrayList<>();
		if(configFilesVO!=null && !configFilesVO.isEmpty()) {
			configFiles = configFilesVO.stream().map
					(n -> { File configFile = new File();
						BeanUtils.copyProperties(n,configFile);
						return configFile;
					}).collect(Collectors.toList());
		}
		return configFiles;
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
								if(n.isIsDeleted()!=null)
									run.setIsDelete(n.isIsDeleted());
								return run;
						}).collect(Collectors.toList());
				data.setRuns(runs);
			}
			if(vo.getComparisons()!=null && !vo.getComparisons().isEmpty()) {
				List<ComparisonDetails> comparisons = vo.getComparisons().stream().map
						(n -> { ComparisonDetails comparison = new ComparisonDetails();
							BeanUtils.copyProperties(n,comparison);
							ComparisonState comparisonState = toComparisonState(n.getState());
							comparison.setComparisonState(comparisonState);
							if(n.isIsDeleted()!=null)
								comparison.setIsDelete(n.isIsDeleted());
							return comparison;
						}).collect(Collectors.toList());
				data.setComparisons(comparisons);
			}
			if(vo.getConfigFiles()!=null && !vo.getConfigFiles().isEmpty()) {
				List<File> files = this.toConfigFiles(vo.getConfigFiles());
				data.setConfigFiles(files);
			}
			data.setBucketId(vo.getBucketId());
			entity.setData(data);
		}
		return entity;
	}
	
	private String toFrequencyParam(String value) {
		switch(value) {
		case "DAILY" : return "Daily";
		case "WEEKLY" : return "Weekly";
		case "MONTHLY" : return "Monthly";
		case "YEARLY" : return "Yearly";
		default: return "";
		}
	}
	
	private com.daimler.data.dto.forecast.RunVO.FrequencyEnum toFrequencyEnum(String value) {
		if(value==null)
			value = "";
		switch(value) {
		case "Daily" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.DAILY;
		case "Weekly" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.WEEKLY;
		case "Monthly" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.MONTHLY;
		case "Yearly" : return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.YEARLY;
		default: return com.daimler.data.dto.forecast.RunVO.FrequencyEnum.NO_FREQUENCY;
		}
	}

	private RunState toState(@Valid RunStateVO stateVO) {
		RunState state = new RunState();
		if(stateVO!=null) {
			if(stateVO.getLifeCycleState()!=null)
				state.setLife_cycle_state(stateVO.getLifeCycleState().name());
			if(stateVO.getResultState()!=null)
				state.setResult_state(stateVO.getResultState().name());
			state.setState_message(stateVO.getStateMessage());
			state.setUser_cancelled_or_timedout(stateVO.isUserCancelledOrTimedout());
		}
		return state;
	}

	private ComparisonState toComparisonState(@Valid ComparisonStateVO comparisonStateVO) {
		ComparisonState comparisonState = new ComparisonState();
		if (comparisonStateVO != null) {
			if (comparisonStateVO.getLifeCycleState() != null)
				comparisonState.setLifeCycleState(comparisonStateVO.getLifeCycleState().name());
			comparisonState.setStateMessage(comparisonStateVO.getStateMessage());

		}
		return comparisonState;
	}



}
