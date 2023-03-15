package com.mb.dna.data.userprivilege.service;

import com.mb.dna.data.api.controller.exceptions.GenericMessage;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeCollectionDto;
import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeResponseDto;

public interface UserPrivilegeService {
	
	UserPrivilegeCollectionDto getAllUsersProfileDetail(int limit, int offset, String sortBy, String sortOrder, String searchTerm);

	GenericMessage bulkAdd(UserPrivilegeCollectionDto collection);

	GenericMessage deleteById(String id);

	UserPrivilegeResponseDto getByShortId(String userId);

	boolean isExist(String id);

}
