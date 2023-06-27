package com.mb.dna.data.dataiku.service;

import java.util.List;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeResponseDto;

public interface DataikuService {

	DataikuProjectsCollectionDto getAllDataikuProjects(String userId, int offset, int limit, String sortBy,
			String sortOrder, String projectName);

	DataikuProjectDto getById(String id);

	DataikuProjectResponseDto createProject(String userId, DataikuProjectDto requestDto, UserPrivilegeResponseDto ownerDetails, List<UserPrivilegeResponseDto> collabPrivilegeDetails);

	GenericMessage deleteById(String id, DataikuProjectDto existingDataikuProject);

	DataikuProjectResponseDto updateProject(String id, DataikuProjectUpdateRequestDto updateRequest,List<UserPrivilegeResponseDto> collabPrivilegeDetails);

	boolean checkExistingProject(String projectName, String cloudProfile);

	DataikuProjectDto getByProjectName(String projectName, String cloudProfile);
	
	DataikuProjectResponseDto provisionSolutionToDataikuProject(String projectName, String cloudProfile,String solutionId);

}
