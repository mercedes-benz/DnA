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

package com.daimler.data.service.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.daimler.data.dto.dashboard.DatasourceWidgetVO;
import com.daimler.data.dto.dashboard.LocationWidgetVO;
import com.daimler.data.dto.dashboard.MilestoneWidgetVO;
import com.daimler.data.dto.dashboard.SolDigitalValuesummaryVO;

public interface DashboardService {

	/**
	 * To fetch count of all the solution having dna Notebook
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
	 * @return count
	 */
	Long getSolCountWithNotebook(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	/**
	 * To get solution count using native query
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
	 * @return solutioncount
	 */
	Long getSolCount(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	/**
	 * To Fetch DataSource Details of solutions
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
	List<DatasourceWidgetVO> getSolDatasource(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin);

	/**
	 * To Fetch solution count at each of the locations
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
	List<LocationWidgetVO> getSolLocation(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin);

	/**
	 * To fetch Milestones of solutions and solution count for each of the
	 * milestones
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
	 * To Fetch digital value of all the solutions
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
	 * @return totalDigitalValue (BigDecimal)
	 */
	BigDecimal getSolDigitalValue(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);

	/**
	 * To Fetch digital value summary for all the solutions
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
	 * @return List<SolDigitalValuesummary>
	 */
	List<SolDigitalValuesummaryVO> getSolDigitalValueSummary(Boolean published, List<String> phases,
			List<String> dataVolumes, String divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> divisionsAdmin);
}
