package com.daimler.data.db.repo.matomo;

import com.daimler.data.db.entities.MatomoNsql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatomoRepository extends JpaRepository<MatomoNsql, String> {

}

