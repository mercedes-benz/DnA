package com.mb.dna.data.userprivilege.db.repo;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.mb.dna.data.userprivilege.api.dto.UserPrivilegeDto;
import com.mb.dna.data.userprivilege.db.entities.UserPrivilegeSql;

public interface UserPrivilegeRepository {

	 	Optional<UserPrivilegeSql> findById(String id);

	    void deleteById(String id);

	    List<UserPrivilegeSql> findAll(int limit, int offset, String sortBy,String sortOrder,String userId);

		UserPrivilegeSql save(UserPrivilegeSql userinfo);

		void update(UserPrivilegeDto record);

		BigInteger findCount(String userId);

		void deleteAll();

		UserPrivilegeSql findByUser(String searchTerm);
	    
}
