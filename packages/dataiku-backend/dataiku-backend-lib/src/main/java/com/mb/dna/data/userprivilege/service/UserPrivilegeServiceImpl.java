package com.mb.dna.data.userprivilege.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.api.controller.exceptions.MessageDescription;
import com.mb.dna.data.assembler.UserPrivilegeAssembler;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeCollectionDto;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeDto;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeResponseDto;
import com.mb.dna.data.userprivilege.db.entities.UserPrivilegeSql;
import com.mb.dna.data.userprivilege.db.repo.UserPrivilegeRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserPrivilegeServiceImpl implements UserPrivilegeService{

	@Inject
	private UserPrivilegeRepository userPrivilegeRepo;
	
	@Inject
	private UserPrivilegeAssembler assembler;
	
	public UserPrivilegeServiceImpl() {
		super();
	}
	
	public UserPrivilegeServiceImpl(UserPrivilegeRepository userPrivilegeRepo, UserPrivilegeAssembler assembler) {
		super();
		this.userPrivilegeRepo = userPrivilegeRepo;
		this.assembler = assembler;
	}

	@Override
	@Transactional
	public UserPrivilegeCollectionDto getAllUsersProfileDetail(int limit, int offset, String sortBy, String sortOrder, String userId) {
		UserPrivilegeCollectionDto response = new UserPrivilegeCollectionDto();
		response.setData(new ArrayList<>());
		response.setTotalCount(new BigInteger("0"));
		List<UserPrivilegeDto> data = new ArrayList<>();
		try {
			List<UserPrivilegeSql> usersData = userPrivilegeRepo.findAll(limit, offset, sortBy, sortOrder,userId);
			data = usersData.stream().map(n -> assembler.toUserPrivilegeVO(n)).collect(Collectors.toList());
			log.info("Fetched userPrivilege records with params limit{} offset{} sortBy{} sortOrder{} userId{} ", limit, offset, sortBy, sortOrder, userId);
		}catch(Exception e) {
			log.error("Failed to fetch userprivilege records with params limit{} offset{} sortBy{} sortOrder{} userId{} ",
					limit, offset, sortBy, sortOrder, userId);
		}
		response.setData(data);
		BigInteger count = userPrivilegeRepo.findCount(userId);
		response.setTotalCount(count);
		return response;
	}
	
	@Override
	@Transactional
	public GenericMessage bulkAdd(UserPrivilegeCollectionDto collection) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		if(collection!=null && collection.getData()!=null && !collection.getData().isEmpty()) {
			try {
				userPrivilegeRepo.deleteAll();
			}catch(Exception e) {
				log.error("Failed to delete existing records with exception {}",e.getMessage());
				MessageDescription noRecordsMsg = new MessageDescription("Failed to delete existing records, might have duplicates. Re-run to cleanup.");
				warnings.add(noRecordsMsg);
			}
			List<UserPrivilegeDto> data = collection.getData();
			List<UserPrivilegeSql> entities = new ArrayList<>();
			entities = data.stream().map(n -> assembler.toUserPrivilegeEntity(n)).collect(Collectors.toList());
			for(UserPrivilegeSql record: entities) {
				try {
					if(record.getUserId()!=null && !record.getUserId().isBlank() && !record.getUserId().isEmpty()) {
						userPrivilegeRepo.save(record);
					}
				}catch(Exception e) {
					log.error("Failed to insert user {} with exception {}",record.getUserId(),e.getMessage());
					MessageDescription noRecordsMsg = new MessageDescription("Failed to insert user "+record.getUserId());
					warnings.add(noRecordsMsg);
				}
			}
			responseMessage.setSuccess("SUCCESS");
		}else {
			responseMessage.setSuccess("SUCCESS");
			MessageDescription noRecordsMsg = new MessageDescription("No content to insert records.");
			warnings.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}
	
	@Override
	@Transactional
	public boolean isExist(String id) {
		boolean flag = false;
		try {
			Optional<UserPrivilegeSql> recordOptional = userPrivilegeRepo.findById(id);
			if(recordOptional.isPresent() && recordOptional!= null && recordOptional.get()!= null && "id".equalsIgnoreCase(recordOptional.get().getId())) {
				flag = true;
			}
		}catch(Exception e) {
			log.error("Failed to fetch if userprivilege record {} exists, with exception {}",id,e.getMessage());
		}
		return flag;
	}
	
	@Override
	@Transactional
	public GenericMessage deleteById(String id) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		try {
			userPrivilegeRepo.deleteById(id);
			responseMessage.setSuccess("SUCCESS");
		}catch(Exception e) {
			log.error("Failed to delete userdetails for record with id {} with exception {}",id,e.getMessage());
			MessageDescription noRecordsMsg = new MessageDescription("Failed to delete userdetails for record "+id);
			errors.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}
	
	@Override
	@Transactional
	public UserPrivilegeResponseDto getByShortId(String userId) {
		UserPrivilegeDto dto = null;
		UserPrivilegeResponseDto responseDto = new UserPrivilegeResponseDto();
		responseDto.setData(null);
		responseDto.setCanCreate(false);
		UserPrivilegeSql userData = userPrivilegeRepo.findByUser(userId);
		if(userData!=null && userData.getId()!=null && userData.getUserId().equalsIgnoreCase(userId) && userData.getProfile()!=null) {
			dto = assembler.toUserPrivilegeVO(userData);
			if(dto!=null) {
				responseDto.setData(dto);
				String profile = dto.getProfile();
				if("DESIGNER".equalsIgnoreCase(profile) || "VISUAL-DESIGNER".equalsIgnoreCase(profile)) {
					responseDto.setCanCreate(true);
				}
			}
		}
		return responseDto;
	}
	
}
