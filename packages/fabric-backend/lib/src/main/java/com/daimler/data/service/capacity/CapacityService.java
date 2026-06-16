package com.daimler.data.service.capacity;

import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.dto.adaProjects.CapacityVO;
import com.daimler.data.service.common.CommonService;

/**
 * Service interface to provide the method for the business logic for the capacity 
 * @author TSATEND
 */
public interface CapacityService extends CommonService<CapacityVO, CapacityNsql, String> {

    /**
     * Get capacity details by region.
     * 
     * @param region the region for which to retrieve capacity details
     * @return CapacityVO containing the capacity details for the specified region or null if not found
     */
    CapacityVO getCapacityByRegion(String region);

    /**
     * Create or update capacity details for a specific region. If capacity details for the region already exist, 
     * they will be updated with the new information provided in the CapacityVO. If no existing capacity details 
     * are found for the region, a new entry will be created.
     * 
     * @param capacityVO the capacity details to be created or updated
     * @param region the region for which to create or update capacity details
     * @return CapacityVO containing the created or updated capacity details
     */
    CapacityVO createOrUpdateCapacity(CapacityVO capacityVO, String region);

    /**
     * Delete capacity details for a specific region. If capacity details for the region exist, they will be deleted
     * and the deleted CapacityVO will be returned. If no capacity details are found for the region, null will be returned.
     * @param region the region for which to delete capacity details
     * @return CapacityVO containing the deleted capacity details or null if not found
     */
    CapacityVO deleteCapacityByRegion(String region);
    
}