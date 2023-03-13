package com.mb.dna.data.userprivilege.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
		response.setTotalcount(new BigInteger("0"));
		List<UserPrivilegeSql> usersData = userPrivilegeRepo.findAll(limit, offset, sortBy, sortOrder,userId);
		List<UserPrivilegeDto> data = usersData.stream().map(n -> assembler.toUserPrivilegeVO(n)).collect(Collectors.toList());
		response.setData(data);
		BigInteger count = userPrivilegeRepo.findCount();
		response.setTotalcount(count);
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
			List<UserPrivilegeDto> data = collection.getData();
			for(UserPrivilegeDto record: data) {
				System.out.println(record.getId());
				try {
					if(record.getUserId()!=null && !record.getUserId().isBlank() && !record.getUserId().isEmpty())
						userPrivilegeRepo.update(record);
					else
						log.warn("Bad request, userid {} was not sent for update", record.getUserId());
				}catch(Exception e) {
					log.error("Failed to update user {} with exception {}",record.getUserId(),e.getMessage());
					MessageDescription noRecordsMsg = new MessageDescription("Failed to update user "+record.getUserId());
					warnings.add(noRecordsMsg);
				}
			}
			responseMessage.setSuccess("SUCCESS");
		}else {
			responseMessage.setSuccess("SUCCESS");
			MessageDescription noRecordsMsg = new MessageDescription("No content to update records.");
			warnings.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
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
		UserPrivilegeResponseDto responseDto = new UserPrivilegeResponseDto();
		responseDto.setData(null);
		responseDto.setCanCreate(false);
		List<UserPrivilegeSql> usersData = userPrivilegeRepo.findAll(1, 0, null, null,userId);
		if(usersData!=null && !usersData.isEmpty()) {
			UserPrivilegeDto dto = assembler.toUserPrivilegeVO(usersData.get(0));
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
