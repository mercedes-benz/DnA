package com.daimler.data.db.repo.capacity;

import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CapacityCustomRepositoryImpl extends CommonDataRepositoryImpl<CapacityNsql, String>
        implements CapacityCustomRepository {
}
