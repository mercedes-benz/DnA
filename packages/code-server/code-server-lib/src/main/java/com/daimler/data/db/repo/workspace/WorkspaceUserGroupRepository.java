package com.daimler.data.db.repo.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.CodeServerUserGroupNsql;

@Repository
public interface WorkspaceUserGroupRepository extends JpaRepository<CodeServerUserGroupNsql, String>{

}
