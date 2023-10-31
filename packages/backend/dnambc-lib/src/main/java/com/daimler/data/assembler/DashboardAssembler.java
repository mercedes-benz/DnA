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

package com.daimler.data.assembler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.dto.SolDigitalValueDTO;
import com.daimler.data.dto.SolDataValueSummaryDTO;
import com.daimler.data.dto.dashboard.DataValueVO;
import com.daimler.data.dto.dashboard.DigitalValueVO;
import com.daimler.data.dto.dashboard.SolDataValueSummaryVO;
import com.daimler.data.dto.dashboard.SolDigitalValuesummaryVO;
import com.daimler.data.util.ConstantsUtility;

@Component
public class DashboardAssembler {

	private static Logger LOGGER = LoggerFactory.getLogger(DashboardAssembler.class);

	public List<String> toList(String parameter) {
		List<String> results = null;
		if (StringUtils.hasText(parameter)) {
			results = new ArrayList<String>();
			String[] parameters = parameter.split(",");
			if (!ObjectUtils.isEmpty(parameters))
				results = Arrays.asList(parameters);
		}
		return results;
	}

	public List<Map<String, List<String>>> toDivisions(String division) {
		List<Map<String, List<String>>> divisions = null;
		if (StringUtils.hasText(division)) {
			divisions = new ArrayList<Map<String, List<String>>>();
			boolean hasEmptySubdivision = false;
			LOGGER.debug("Checking for EMPTY subdivision in query");
			if (division.contains(ConstantsUtility.EMPTY_VALUE)) {
				hasEmptySubdivision = true;
			}
			Map<String, List<String>> divisionMap = new HashMap<String, List<String>>();
			List<String> subdivisionsList = new ArrayList<String>();
			division = division.substring(1, division.length() - 1);
			String[] divisionSplit = division.split("},", 0);
			for (int i = 0; i < divisionSplit.length; i++) {
				divisionSplit[i] = divisionSplit[i].replaceAll("[\\{\\}\\[\\]]", "");
				String[] divSubdivSplitArray = divisionSplit[i].split(",");
				subdivisionsList = new ArrayList<String>();
				divisionMap = new HashMap<String, List<String>>();
				if (null != divSubdivSplitArray) {
					if (divSubdivSplitArray.length > 1) {
						for (int j = 1; j < divSubdivSplitArray.length; j++) {
							subdivisionsList.add(divSubdivSplitArray[j]);
						}
					}
					if (hasEmptySubdivision && !subdivisionsList.contains(ConstantsUtility.EMPTY_VALUE)) {
						LOGGER.debug("Appending EMPTY in subdivisionList");
						subdivisionsList.add(ConstantsUtility.EMPTY_VALUE);
					}
					divisionMap.put(divSubdivSplitArray[0], subdivisionsList);
				}
				divisions.add(divisionMap);
			}
		}

		return divisions;
	}

	/**
	 * To convert solDigitalValues to digital value summary response
	 * 
	 * @param treeMap
	 * @return List<SolDigitalValuesummaryVO>
	 */
	public List<SolDigitalValuesummaryVO> toDigitalValueSummary(
			Map<BigDecimal, SortedSet<SolDigitalValueDTO>> digitalValueSummaryTreeMap) {
		List<SolDigitalValuesummaryVO> solDigitalValuesummary = new ArrayList<SolDigitalValuesummaryVO>();
		SolDigitalValuesummaryVO solDigitalValuesummaryVO = null;
		for (Map.Entry<BigDecimal, SortedSet<SolDigitalValueDTO>> map : digitalValueSummaryTreeMap.entrySet()) {
			solDigitalValuesummaryVO = new SolDigitalValuesummaryVO();
			solDigitalValuesummaryVO.setYear(map.getKey());
			solDigitalValuesummaryVO.setDigitalValueVO(this.toDigitalValueVO(map.getValue()));
			solDigitalValuesummary.add(solDigitalValuesummaryVO);
		}
		return solDigitalValuesummary;
	}

	/*
	 * To convert solDigitalValues to digital value.
	 * 
	 * @param solDigitalValues(SortedSet<SolDigitalValueDTO>)
	 * 
	 * @return List<DigitalValueVO>
	 */
	private List<DigitalValueVO> toDigitalValueVO(SortedSet<SolDigitalValueDTO> solDigitalValues) {
		List<DigitalValueVO> digitalValues = new ArrayList<DigitalValueVO>();
		DigitalValueVO digitalValueVO = null;
		for (SolDigitalValueDTO vo : solDigitalValues) {
			digitalValueVO = new DigitalValueVO();
			digitalValueVO.setSolutionId(vo.getId());
			digitalValueVO.setProductName(vo.getProductName());
			digitalValueVO.setDigitalValue(vo.getCalculatedDigitalValueVO().getValue());
			digitalValues.add(digitalValueVO);
		}
		return digitalValues;
	}

	public List<SolDataValueSummaryVO> toDataValueSummary(
			Map<BigDecimal, Set<SolDataValueSummaryDTO>> dataValueSummaryTreeMap) {
		List<SolDataValueSummaryVO> solDataValuesummary = new ArrayList<SolDataValueSummaryVO>();
		SolDataValueSummaryVO solDataValueSummaryVO = null;
		for(Map.Entry<BigDecimal, Set<SolDataValueSummaryDTO>> map : dataValueSummaryTreeMap.entrySet()) {
			solDataValueSummaryVO = new SolDataValueSummaryVO();
			solDataValueSummaryVO.setYear(map.getKey());
			solDataValueSummaryVO.setDataValueVO(this.toDataValueVO(map.getValue()));
			solDataValuesummary.add(solDataValueSummaryVO);
		}
		return solDataValuesummary;
	}

	private List<DataValueVO> toDataValueVO(Set<SolDataValueSummaryDTO> solDataValues) {
		List<DataValueVO> dataValues = new ArrayList<DataValueVO>();
		DataValueVO dataValueVO = null;
		for(SolDataValueSummaryDTO vo : solDataValues) {
			dataValueVO = new DataValueVO();
			dataValueVO.setSolutionId(vo.getId());
			dataValueVO.setProductName(vo.getProductName());
			dataValueVO.setSavings(vo.getSavings());
			dataValueVO.setRevenue(vo.getRevenue());
			//dataValueVO.setSavings(vo.getSavings());
			dataValues.add(dataValueVO);
		}
		return dataValues;
	}


}
