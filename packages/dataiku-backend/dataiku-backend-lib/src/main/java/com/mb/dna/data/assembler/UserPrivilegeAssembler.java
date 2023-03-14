package com.mb.dna.data.assembler;

import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeDto;
import com.mb.dna.data.userprivilege.db.entities.UserPrivilegeSql;

import jakarta.inject.Singleton;

@Singleton
public class UserPrivilegeAssembler {

	public UserPrivilegeDto toUserPrivilegeVO(UserPrivilegeSql userDetails) {
		UserPrivilegeDto data = new UserPrivilegeDto();
		if(userDetails!=null) {
			data.setId(userDetails.getId());
			data.setUserId(userDetails.getUserId());
			data.setProfile(userDetails.getProfile());
			data.setGivenName(userDetails.getGivenName());
			data.setSurName(userDetails.getSurName());
		}
		return data;
	}
	
}
