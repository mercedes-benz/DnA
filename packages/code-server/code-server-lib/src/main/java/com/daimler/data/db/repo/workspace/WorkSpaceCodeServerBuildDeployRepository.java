package com.daimler.data.db.repo.workspace;

import com.daimler.data.db.entities.CodeServerBuildDeployNsql;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSpaceCodeServerBuildDeployRepository extends JpaRepository<CodeServerBuildDeployNsql,String> {

}
