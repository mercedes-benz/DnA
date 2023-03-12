package com.mb.dna.data.service;

import com.mb.dna.data.controller.exceptions.GenericMessage;
import com.mb.dna.data.controller.userprivilege.UserPrivilegeCollectionDto;

public interface UserPrivilegeService {

	UserPrivilegeCollectionDto getAllUsersProfileDetail(int limit, int offset, String sortBy, String sortOrder);

	GenericMessage bulkAdd(UserPrivilegeCollectionDto collection);

}
