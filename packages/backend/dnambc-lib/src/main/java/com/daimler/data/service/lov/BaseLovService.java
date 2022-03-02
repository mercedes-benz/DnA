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

package com.daimler.data.service.lov;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.LovAssembler;
import com.daimler.data.assembler.SolutionAssembler;
import com.daimler.data.db.entities.lov.BenefitRelevanceNsql;
import com.daimler.data.db.entities.lov.BusinessGoalNsql;
import com.daimler.data.db.entities.lov.CategoryNsql;
import com.daimler.data.db.entities.lov.MaturityLevelNsql;
import com.daimler.data.db.entities.lov.StrategicRelevanceNsql;
import com.daimler.data.db.entities.lov.DataStrategyDomainNsql;
import com.daimler.data.db.repo.lov.LovRepository;
import com.daimler.data.db.repo.solution.SolutionRepository;
import com.daimler.data.dto.lov.BenefitRelevanceVO;
import com.daimler.data.dto.lov.BusinessGoalVO;
import com.daimler.data.dto.lov.CategoryVO;
import com.daimler.data.dto.lov.DataStrategyDomainVO;
import com.daimler.data.dto.lov.MaturityLevelVO;
import com.daimler.data.dto.lov.StrategicRelevanceVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.tag.TagService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseLovService implements LovService {

	@Autowired
	private LovRepository lovRepository;

	@Autowired
	private LovAssembler lovAssembler;

	public BaseLovService() {
		super();
	}

	/**
	 * getAllBusinessGoal
	 * <P>
	 * Fetch all valid Business Goal
	 * 
	 * @return List<BusinessGoalVO>
	 * 
	 */
	@Override
	@Transactional
	public List<BusinessGoalVO> getAllBusinessGoal() {
		List<BusinessGoalNsql> businessGoals = lovRepository.getAllBusinessGoal();
		return lovAssembler.toBusinessGoalVoList(businessGoals);
	}

	/**
	 * getAllBenefitRelevance
	 * <P>
	 * Fetch all valid Benefit Relevance
	 * 
	 * @return List<BenefitRelevanceVO>
	 * 
	 */
	@Override
	@Transactional
	public List<BenefitRelevanceVO> getAllBenefitRelevance() {
		List<BenefitRelevanceNsql> benefitRelevances = lovRepository.getAllBenefitRelevance();
		return lovAssembler.toBenefitRelevanceVOList(benefitRelevances);
	}

	/**
	 * getAllStrategicRelevance
	 * <P>
	 * Fetch all valid Strategic Relevance
	 * 
	 * @return List<StrategicRelevanceVO>
	 * 
	 */
	@Override
	@Transactional
	public List<StrategicRelevanceVO> getAllStrategicRelevance() {
		List<StrategicRelevanceNsql> strategicRelevances = lovRepository.getAllStrategicRelevance();
		return lovAssembler.toStrategicRelevanceVOList(strategicRelevances);
	}

	/**
	 * getAllMaturityLevel
	 * <P>
	 * Fetch all valid Maturity level
	 * 
	 * @return List<MaturityLevelVO>
	 * 
	 */
	@Override
	@Transactional
	public List<MaturityLevelVO> getAllMaturityLevel() {
		List<MaturityLevelNsql> maturityLevels = lovRepository.getAllMaturityLevel();
		return lovAssembler.toMaturityLevelVOList(maturityLevels);
	}

	/**
	 * getAllCategory
	 * <P>
	 * Fetch all valid Category
	 * 
	 * @return List<CategoryVO>
	 * 
	 */
	@Override
	@Transactional
	public List<CategoryVO> getAllCategory() {
		List<CategoryNsql> categories = lovRepository.getAllCategory();
		return lovAssembler.toCategoryVOList(categories);
	}

	@Override
	public List<DataStrategyDomainVO> getAllStrategyDomain() {
		List<DataStrategyDomainNsql> strategyDomains = lovRepository.getAllStrategyDomain();
		return lovAssembler.toStrategyDomainVOList(strategyDomains);
	}

}
