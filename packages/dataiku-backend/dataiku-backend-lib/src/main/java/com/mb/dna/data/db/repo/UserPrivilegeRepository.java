package com.mb.dna.data.db.repo;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.mb.dna.data.controller.userprivilege.UserPrivilegeDto;
import com.mb.dna.data.db.entities.UserPrivilegeSql;

public interface UserPrivilegeRepository {

	 	Optional<UserPrivilegeSql> findById(String id);

	    void deleteById(String id);

	    List<UserPrivilegeSql> findAll(int limit, int offset, String sortBy,String sortOrder,String userId);

		UserPrivilegeSql save(UserPrivilegeSql userinfo);

		BigInteger findCount();

		void update(UserPrivilegeDto record);
	    
}
