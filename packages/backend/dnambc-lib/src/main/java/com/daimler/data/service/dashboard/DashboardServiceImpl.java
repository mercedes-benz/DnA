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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.db.repo.solution.SolutionCustomRepository;
import com.daimler.data.dto.dashboard.DatasourceWidgetVO;
import com.daimler.data.dto.dashboard.LocationWidgetVO;
import com.daimler.data.dto.dashboard.MilestoneWidgetVO;

@Service
public class DashboardServiceImpl implements DashboardService {

	private Logger LOGGER = LoggerFactory.getLogger(DashboardServiceImpl.class);

	@Autowired
	private SolutionCustomRepository customRepo;

	@Override
	public Long getSolCountWithNotebook(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		LOGGER.trace("Entering getSolCountWithNotebook.");
		return customRepo.getSolCountWithNotebook(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
	}

	@Override
	public List<DatasourceWidgetVO> getSolDatasource(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		LOGGER.trace("Entering getSolDatasource.");
		List<DatasourceWidgetVO> res = customRepo.getSolutionDataVolume(published, phases, dataVolumes, divisions,
				locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
		LOGGER.trace("Returning from getSolDatasource.");
		return res;
	}

	@Override
	public List<LocationWidgetVO> getSolLocation(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		LOGGER.trace("Entering getSolLocation.");
		List<LocationWidgetVO> res = customRepo.getSolutionLocations(published, phases, dataVolumes, divisions,
				locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
		LOGGER.trace("Returning from getSolLocation.");
		return res;
	}

	@Override
	public List<MilestoneWidgetVO> getSolMilestone(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		LOGGER.trace("Entering getSolMilestone.");
		List<MilestoneWidgetVO> res = customRepo.getSolMilestone(published, phases, dataVolumes, divisions, locations,
				statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
		LOGGER.trace("Returning from getSolMilestone.");
		return res;
	}

	@Override
	public BigDecimal getSolDigitalValue(Boolean published, List<String> phases,
			List<String> dataVolumes, List<Map<String, List<String>>> divisions, List<String> locations,
			List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags) {
		LOGGER.trace("Entering getSolDigitalValue.");
		return customRepo.getDigitalValuesSum(published, phases, dataVolumes, divisions,
				locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
	}

	@Override
	public Long getSolCount(Boolean published, List<String> phases, List<String> dataVolumes,
			List<Map<String, List<String>>> divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags) {
		return customRepo.getCountUsingNativeQuery(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags);
	}

}
