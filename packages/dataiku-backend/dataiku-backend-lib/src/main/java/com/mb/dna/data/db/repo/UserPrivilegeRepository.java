package com.mb.dna.data.db.repo;

import java.util.List;
import java.util.Optional;

import com.mb.dna.data.db.entities.UserPrivilege;
import com.mb.dna.data.db.entities.UserPrivilegeNsql;

public interface UserPrivilegeRepository {

	 	Optional<UserPrivilegeNsql> findById(long id);

	    void deleteById(long id);

	    List<UserPrivilegeNsql> findAll(int limit, int offset, String sortBy,String sortOrder,String userId);

	    int update(long id,String profile);

		UserPrivilegeNsql save(UserPrivilege userinfo);

		Integer findCount();

		void update(String userId, String profile);
	    
}
