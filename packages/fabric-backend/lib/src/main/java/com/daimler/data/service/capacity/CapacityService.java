package com.daimler.data.service.capacity;

import java.util.List;

import com.daimler.data.db.entities.CapacityNsql;
import com.daimler.data.dto.adaProjects.CapacityVO;
import com.daimler.data.service.common.CommonService;

/**
 * Service interface to provide the method for the business logic for the capacity 
 * @author TSATEND
 */
public interface CapacityService extends CommonService<CapacityVO, CapacityNsql, String> {

    /**
     * Fetch the capacity details from microsoft fabric by capacity id.
     * this method will fetch the capacity details from the microsoft fabric for the given capacity id and
     * return it as a CapacityVO object.
     * 
     * @param capacityId the capacity id for which to retrieve capacity details
     * @return CapacityVO containing the capacity details for the specified capacity id or null if not found
     */
    CapacityVO getCapacityById(String capacityId);

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

    /**
     * Get capacity details for all regions. This method retrieves the capacity details for all regions and returns them as a 
     * list of CapacityVO objects. If no capacity details are found, an empty list will be returned.
     * @return List<CapacityVO> List of CapacityVO objects containing the capacity details for all regions
     */
    List<CapacityVO> getAllCapacity();

    /**
     * get all the regions for which capacity details can be configured. 
     * this method will fetch the list of regions from the vault and return them as a list of strings.
     * @return List<String> List of regions for which capacity details can be configured
     */
    List<String> getAllRegions();
}