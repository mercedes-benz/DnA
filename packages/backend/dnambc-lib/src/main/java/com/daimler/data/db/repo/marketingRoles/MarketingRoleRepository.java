package com.daimler.data.db.repo.marketingRoles;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.data.db.entities.MarketingRoleNsql;

public interface MarketingRoleRepository extends JpaRepository<MarketingRoleNsql, String> {

}
