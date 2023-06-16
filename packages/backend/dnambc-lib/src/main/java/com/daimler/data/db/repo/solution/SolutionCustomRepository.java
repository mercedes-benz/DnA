/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.db.repo.solution;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.daimler.data.db.entities.SolutionNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.dto.SolDigitalValueDTO;
import com.daimler.data.dto.dashboard.DatasourceWidgetVO;
import com.daimler.data.dto.dashboard.LocationWidgetVO;
import com.daimler.data.dto.dashboard.MilestoneWidgetVO;

public interface SolutionCustomRepository extends CommonDataRepository<SolutionNsql, String> {

	/**
	 * To get all the solution with filter.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param relatedProducts
	 * @param offset
	 * @param limit
	 * @param sortBy
	 * @param sortOrder
	 * @return List<SolutionNsql>
	 */
	List<SolutionNsql> getAllWithFilters(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> relatedProducts, int offset, int limit,
			String sortBy, String sortOrder);

	/**
	 * To get Solution count with filter.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @return solutionCount{Long}
	 */
	Long getCount(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags);

	/**
	 * To get All solution with filter using native query.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param relatedProducts
	 * @param divisionsAdmin
	 * @param offset
	 * @param limit
	 * @param sortBy
	 * @param sortOrder
	 * @return List<SolutionNsql>
	 */
	List<SolutionNsql> getAllWithFiltersUsingNativeQuery(Boolean published, List<String> phases,
			List<String> dataVolumes, String divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> relatedProducts, List<String> divisionsAdmin,
			Boolean hasDigitalValue, Boolean hasNotebook, int offset, int limit, String sortBy, String sortOrder);

	/**
	 * To get SOlution count using Native query.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return solutionCount{Long}
	 */
	Long getCountUsingNativeQuery(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin,
			Boolean hasDigitalValue, Boolean hasNotebook);

	/**
	 * To get count of all the solutions which have Notebook Associated.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return count{Long}
	 */
	Long getSolCountWithNotebook(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	/**
	 * To get Sum of all the digital values of the solutions.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return sum{BigDecimal}
	 */
	BigDecimal getDigitalValuesSum(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	/**
	 * To get Data volume of all the solutions.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return List<DatasourceWidgetVO>
	 */
	List<DatasourceWidgetVO> getSolutionDataVolume(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin);

	/**
	 * To get Location wise solutions
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return List<LocationWidgetVO>
	 */
	List<LocationWidgetVO> getSolutionLocations(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin);

	/**
	 * To get Milestones of all the solutions.
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return List<MilestoneWidgetVO>
	 */
	List<MilestoneWidgetVO> getSolMilestone(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin);

	/**
	 * To Fetch Digital Value summary
	 * 
	 * @param published
	 * @param phases
	 * @param dataVolumes
	 * @param divisions
	 * @param locations
	 * @param statuses
	 * @param solutionType
	 * @param userId
	 * @param isAdmin
	 * @param bookmarkedSolutions
	 * @param searchTerms
	 * @param tags
	 * @param divisionsAdmin
	 * @return List<SolDigitalValueDTO>
	 */
	List<SolDigitalValueDTO> getDigitalValueUsingNativeQuery(Boolean published, List<String> phases,
			List<String> dataVolumes, String divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	Integer getCountBasedPublishSolution(Boolean published);
}
