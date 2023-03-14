package com.mb.dna.data.dataiku.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.api.controller.exceptions.MessageDescription;
import com.mb.dna.data.assembler.DataikuAssembler;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectResponseDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectUpdateRequestDto;
import com.mb.dna.data.dataiku.api.dto.DataikuProjectsCollectionDto;
import com.mb.dna.data.dataiku.db.entities.DataikuSql;
import com.mb.dna.data.dataiku.db.repo.DataikuRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DataikuServiceImpl implements DataikuService	{

	@Inject
	private DataikuRepository dataikuRepo;
	
	@Inject
	private DataikuAssembler assembler;
	
	public DataikuServiceImpl() {
		super();
	}
	
	public DataikuServiceImpl(DataikuRepository dataikuRepo, DataikuAssembler assembler) {
		super();
		this.dataikuRepo = dataikuRepo;
		this.assembler = assembler;
	}
	
	@Override
	@Transactional
	public DataikuProjectsCollectionDto getAllDataikuProjects(String userId, int offset, int limit, String sortBy, String sortOrder, String projectName) {
		DataikuProjectsCollectionDto response = new DataikuProjectsCollectionDto();
		response.setData(new ArrayList<>());
		response.setTotalcount(new BigInteger("0"));
		List<DataikuSql> projects = dataikuRepo.getAll(userId, offset, limit, sortBy, sortOrder, projectName);
		List<DataikuProjectDto> data = projects.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		response.setData(data);
		BigInteger count = dataikuRepo.getTotalCount(userId, projectName);
		response.setTotalcount(count);
		return response;
	}
	
	@Override
	@Transactional
	public DataikuProjectDto getById(String id) {
		DataikuProjectDto dto = new DataikuProjectDto();
		Optional<DataikuSql> result = dataikuRepo.findById(id);
		if(result!=null && result.get()!=null) {
			dto = assembler.toVo(result.get());
			try {
				ObjectMapper mapper = new ObjectMapper();
				System.out.println(mapper.writeValueAsString(dto));
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return dto;
	}
	
	@Override
	@Transactional
	public DataikuProjectResponseDto updateProject(String id,DataikuProjectUpdateRequestDto updateRequest) {
		DataikuProjectUpdateDto updateData = updateRequest.getData();
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto existingRecord = this.getById(id);
		existingRecord.setCollaborators(updateData.getCollaborators());
		if(updateData.getDescription()!=null)
			existingRecord.setDescription(updateData.getDescription());
		try {
			DataikuSql entity = assembler.toEntity(existingRecord);
			dataikuRepo.update(entity);
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to update dataiku project {} with exception {}", existingRecord.getProjectName(), e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to save new dataiku project " + existingRecord.getProjectName() 
			+  " with exception " + e.getMessage());
			errors.add(errMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseWrapperDto.setResponse(responseMessage);
		responseWrapperDto.setData(existingRecord);
		return responseWrapperDto;
			
	}
	
	@Override
	@Transactional
	public DataikuProjectResponseDto createProject(String userId, DataikuProjectDto requestDto) {
		DataikuProjectResponseDto responseWrapperDto = new DataikuProjectResponseDto();
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		DataikuProjectDto dto = requestDto;
		try {
			ObjectMapper mapper = new ObjectMapper();
			DataikuSql entity = new DataikuSql();
			requestDto.setCreatedBy(userId);
			requestDto.setCreatedOn(new Date());
			String id = UUID.randomUUID().toString();
			requestDto.setId(id);
			System.out.println("requestdto is " + mapper.writeValueAsString(requestDto));
			entity = assembler.toEntity(requestDto);
			System.out.println("entity is " + mapper.writeValueAsString(entity));
			System.out.println(entity.getCollaborators().size());
			System.out.println("collab is " + mapper.writeValueAsString(entity.getCollaborators().get(0)));
			dataikuRepo.save(entity);
			dto = requestDto;
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to save new dataiku project {} with exception {}", requestDto.getProjectName(), e.getMessage());
			MessageDescription errMsg = new MessageDescription("Failed to save new dataiku project " + requestDto.getProjectName() 
			+  " with exception " + e.getMessage());
			errors.add(errMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseWrapperDto.setResponse(responseMessage);
		responseWrapperDto.setData(dto);
		return responseWrapperDto;
	}
	
	@Override
	@Transactional
	public GenericMessage deleteById(String id) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			dataikuRepo.deleteById(id);
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to delete dataiku project with id {} and exception {}",id,e.getMessage());
			MessageDescription noRecordsMsg = new MessageDescription("Failed to delete dataiku project with exception " + e.getMessage());
			errors.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}
	
	
}
