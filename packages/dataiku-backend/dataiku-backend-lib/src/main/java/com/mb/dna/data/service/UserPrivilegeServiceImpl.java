package com.mb.dna.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mb.dna.data.controller.exceptions.GenericMessage;
import com.mb.dna.data.controller.exceptions.MessageDescription;
import com.mb.dna.data.controller.userprivilege.UserPrivilegeCollectionDto;
import com.mb.dna.data.controller.userprivilege.UserPrivilegeDto;
import com.mb.dna.data.db.entities.UserPrivilegeNsql;
import com.mb.dna.data.db.repo.UserPrivilegeRepository;
import com.mb.dna.data.utility.UserPrivilegeAssembler;

import jakarta.inject.Singleton;

@Singleton
public class UserPrivilegeServiceImpl implements UserPrivilegeService{

	private UserPrivilegeRepository userPrivilegeRepo;
	private UserPrivilegeAssembler assembler;
	
	@Override
	public UserPrivilegeCollectionDto getAllUsersProfileDetail(int limit, int offset, String sortBy, String sortOrder) {
		UserPrivilegeCollectionDto response = new UserPrivilegeCollectionDto();
		response.setData(new ArrayList<>());
		response.setTotalcount(0);
		List<UserPrivilegeNsql> usersData = userPrivilegeRepo.findAll(limit, offset, sortBy, sortOrder,null);
		List<UserPrivilegeDto> data = usersData.stream().map(n -> assembler.toUserPrivilegeVO(n)).collect(Collectors.toList());
		response.setData(data);
		Integer count = userPrivilegeRepo.findCount();
		response.setTotalcount(count);
		return response;
	}
	
	@Override
	public GenericMessage bulkAdd(UserPrivilegeCollectionDto collection) {
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		if(collection!=null && collection.getData()!=null && !collection.getData().isEmpty()) {
			List<UserPrivilegeDto> data = collection.getData();
			for(UserPrivilegeDto record: data) {
				UserPrivilegeNsql entity = userPrivilegeRepo.findAll(0, 0, null, null,record.getUserId()).get(0);
				if(entity!=null && entity.getId()!=null && record.getUserId().equalsIgnoreCase(entity.getData().getUserId())) {
					entity.getData().setProfile(record.getProfile());
				}
			}
		}else {
			responseMessage.setSuccess("SUCCESS");
			MessageDescription noRecordsMsg = new MessageDescription("No content to update records.");
			warnings.add(noRecordsMsg);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		return responseMessage;
	}
	
}
