package com.mb.dna.data.utility;

import com.mb.dna.data.controller.userprivilege.UserPrivilegeDto;
import com.mb.dna.data.db.entities.UserPrivilegeSql;

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
