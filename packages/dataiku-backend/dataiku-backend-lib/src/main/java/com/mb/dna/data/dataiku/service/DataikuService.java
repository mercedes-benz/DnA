package com.mb.dna.data.dataiku.service;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;

public interface DataikuService {

	DataikuProjectsCollectionDto getAllDataikuProjects(String userId, int offset, int limit, String sortBy,
			String sortOrder, String projectName);

	DataikuProjectDto getById(String id);

	DataikuProjectResponseDto createProject(String userId, DataikuProjectDto requestDto);

	GenericMessage deleteById(String id);

	DataikuProjectResponseDto updateProject(String id, DataikuProjectUpdateRequestDto updateRequest);

}
