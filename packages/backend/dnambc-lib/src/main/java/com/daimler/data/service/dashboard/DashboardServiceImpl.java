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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.assembler.DashboardAssembler;
import com.daimler.data.db.jsonb.solution.DataValueRampUpYear;
import com.daimler.data.db.repo.solution.SolutionCustomRepository;
import com.daimler.data.dto.SolDataValueDTO;
import com.daimler.data.dto.SolDigitalValueDTO;
import com.daimler.data.dto.SolDataValueSummaryDTO;
import com.daimler.data.dto.dashboard.DataValueVO;
import com.daimler.data.dto.dashboard.DatasourceWidgetVO;
import com.daimler.data.dto.dashboard.LocationWidgetVO;
import com.daimler.data.dto.dashboard.MilestoneWidgetVO;
import com.daimler.data.dto.dashboard.SolDataValueSummaryVO;
import com.daimler.data.dto.dashboard.SolDigitalValuesummaryVO;

@Service
public class DashboardServiceImpl implements DashboardService {

	private Logger LOGGER = LoggerFactory.getLogger(DashboardServiceImpl.class);

	@Autowired
	private SolutionCustomRepository customRepo;

	@Autowired
	private DashboardAssembler dashboardAssembler;

	@Override
	public Long getSolCountWithNotebook(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getSolCountWithNotebook(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	@Override
	public List<DatasourceWidgetVO> getSolDatasource(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getSolutionDataVolume(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	@Override
	public List<LocationWidgetVO> getSolLocation(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getSolutionLocations(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	@Override
	public List<MilestoneWidgetVO> getSolMilestone(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getSolMilestone(published, phases, dataVolumes, divisions, locations, statuses, solutionType,
				userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	@Override
	public BigDecimal getSolDigitalValue(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getDigitalValuesSum(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	@Override
	public Long getSolCount(Boolean published, List<String> phases, List<String> dataVolumes, String divisions,
			List<String> locations, List<String> statuses, String solutionType, String userId, Boolean isAdmin,
			List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getCountUsingNativeQuery(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin, false, false);
	}

	@Override
	public List<SolDigitalValuesummaryVO> getSolDigitalValueSummary(Boolean published, List<String> phases,
			List<String> dataVolumes, String divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> divisionsAdmin) {
		List<SolDigitalValueDTO> result = customRepo.getDigitalValueUsingNativeQuery(published, phases, dataVolumes,
				divisions, locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags,
				divisionsAdmin);
		SortedSet<SolDigitalValueDTO> digitalValueSortedSet = null;
		Map<BigDecimal, SortedSet<SolDigitalValueDTO>> digitalValueSummaryTreeMap = new TreeMap<BigDecimal, SortedSet<SolDigitalValueDTO>>();
		for (SolDigitalValueDTO dto : result) {
			if (dto.getCalculatedDigitalValueVO() != null && dto.getCalculatedDigitalValueVO().getYear() != null) {
				if (!digitalValueSummaryTreeMap.containsKey(dto.getCalculatedDigitalValueVO().getYear())) {
					digitalValueSortedSet = new TreeSet<SolDigitalValueDTO>(digitalValueComp);
					digitalValueSortedSet.add(dto);
					digitalValueSummaryTreeMap.put(dto.getCalculatedDigitalValueVO().getYear(), digitalValueSortedSet);
				} else {
					digitalValueSummaryTreeMap.get(dto.getCalculatedDigitalValueVO().getYear()).add(dto);
				}
			}
		}
		return dashboardAssembler.toDigitalValueSummary(digitalValueSummaryTreeMap);
	}

	/**
	 * Comparator to sort Digital Value DTO based on digital values
	 * 
	 */
	private Comparator<SolDigitalValueDTO> digitalValueComp = new Comparator<SolDigitalValueDTO>() {
		@Override
		public int compare(SolDigitalValueDTO s1, SolDigitalValueDTO s2) {
			if (s2.getCalculatedDigitalValueVO().getValue()
					.compareTo(s1.getCalculatedDigitalValueVO().getValue()) == 0) {
				return 1;
			} else {
				return (s2.getCalculatedDigitalValueVO().getValue()
						.compareTo(s1.getCalculatedDigitalValueVO().getValue()));
			}
		}
	};
	

	@Override
	public List<BigDecimal> getSolDataValue(Boolean published, List<String> phases, List<String> dataVolumes,
			String divisions, List<String> locations, List<String> statuses, String solutionType, String userId,
			Boolean isAdmin, List<String> bookmarkedSolutions, List<String> searchTerms, List<String> tags,
			List<String> divisionsAdmin) {
		return customRepo.getDataValuesSum(published, phases, dataVolumes, divisions, locations, statuses,
				solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags, divisionsAdmin);
	}

	
	@Override
	public List<SolDataValueSummaryVO> getSolDataValueSummary(Boolean published, List<String> phases,
			List<String> dataVolumes, String divisions, List<String> locations, List<String> statuses,
			String solutionType, String userId, Boolean isAdmin, List<String> bookmarkedSolutions,
			List<String> searchTerms, List<String> tags, List<String> divisionsAdmin) {

		List<SolDataValueDTO> result = customRepo.getDataValueUsingNativeQuery(published, phases, dataVolumes,
				divisions, locations, statuses, solutionType, userId, isAdmin, bookmarkedSolutions, searchTerms, tags,
				divisionsAdmin);
		HashMap<BigDecimal, List<DataValueVO>> finalMap = new HashMap<BigDecimal, List<DataValueVO>>();// Creating HashMap

		for (SolDataValueDTO dto : result) {
			List<DataValueRampUpYear> savingsList =  dto.getSavings();
			List<DataValueRampUpYear> revenueList =  dto.getRevenue();
			if(Objects.nonNull(savingsList) && savingsList.size() > 0 ) {
				for(DataValueRampUpYear saving : savingsList) {
					List<DataValueVO> existingVal = finalMap.get(saving.getYear());
					if(Objects.nonNull(existingVal) && existingVal.size() > 0) {
						DataValueVO dataValueVO = new DataValueVO();
						dataValueVO.setSolutionId(dto.getId());
						dataValueVO.setProductName(dto.getProductName());
						dataValueVO.setSavings(saving.getValue());
						dataValueVO.setRevenue(BigDecimal.ZERO);
						existingVal.add(dataValueVO);
						finalMap.put(saving.getYear(), existingVal);
					}
					else {
						List<DataValueVO> newVal = new ArrayList<>();
						DataValueVO dataValueVO = new DataValueVO();
						dataValueVO.setSolutionId(dto.getId());
						dataValueVO.setProductName(dto.getProductName());
						dataValueVO.setSavings(saving.getValue());
						dataValueVO.setRevenue(BigDecimal.ZERO);
						newVal.add(dataValueVO);
						finalMap.put(saving.getYear(), newVal);
					}
				}
			}
			
			if(Objects.nonNull(revenueList) && revenueList.size() > 0 ) {
				for(DataValueRampUpYear revenue : revenueList) {
					List<DataValueVO> existingValues = finalMap.get(revenue.getYear());
					if(existingValues != null && existingValues.size() > 0) {
						DataValueVO dataValueVO = existingValues.stream().filter(n->n.getSolutionId() == dto.getId()).findAny().orElse(null);
						if(Objects.nonNull(dataValueVO)) {
							dataValueVO.setRevenue(revenue.getValue());
							existingValues.add(dataValueVO);
						}
						else {
							DataValueVO newDataValueVO = new DataValueVO();
							newDataValueVO.setSolutionId(dto.getId());
							newDataValueVO.setProductName(dto.getProductName());
							newDataValueVO.setSavings(BigDecimal.ZERO);
							newDataValueVO.setRevenue(revenue.getValue());
							existingValues.add(newDataValueVO);
						}
						List<DataValueVO> newList = existingValues.stream() 
                                .distinct() 
                                .collect(Collectors.toList()); 
						finalMap.put(revenue.getYear(), newList);
					}
					else {
						List<DataValueVO> newValue = new ArrayList<>();
						DataValueVO dataValueVO = new DataValueVO();
						dataValueVO.setSolutionId(dto.getId());
						dataValueVO.setProductName(dto.getProductName());
						dataValueVO.setSavings(BigDecimal.ZERO);
						dataValueVO.setRevenue(revenue.getValue());
						newValue.add(dataValueVO);
						finalMap.put(revenue.getYear(), newValue);
					}
				}
			}
		}
		return dashboardAssembler.toDataValueSummary(finalMap);

	}


}
