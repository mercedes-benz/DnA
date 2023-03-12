package com.mb.dna.data.utility;

import com.mb.dna.data.controller.userprivilege.UserPrivilegeDto;
import com.mb.dna.data.db.entities.UserPrivilegeNsql;

import jakarta.inject.Singleton;

@Singleton
public class UserPrivilegeAssembler {

	public UserPrivilegeDto toUserPrivilegeVO(UserPrivilegeNsql userDetails) {
		UserPrivilegeDto data = new UserPrivilegeDto();
		if(userDetails!=null && userDetails.getData()!=null) {
			data.setId(userDetails.getId());
			data.setUserId(userDetails.getData().getUserId());
			data.setProfile(userDetails.getData().getProfile());
		}
		return data;
	}
}
