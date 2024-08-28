package com.daimler.data.db.repo.workspace;


import org.springframework.data.jpa.repository.JpaRepository;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;

public interface WorkspaceAdditionalServiceRepo extends JpaRepository<CodeServerAdditionalServiceNsql, String>{
    
}
