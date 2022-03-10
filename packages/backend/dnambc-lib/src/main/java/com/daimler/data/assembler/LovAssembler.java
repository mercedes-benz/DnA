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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.data.db.entities.lov.AdditionalResourceNsql;
import com.daimler.data.db.entities.lov.BenefitRelevanceNsql;
import com.daimler.data.db.entities.lov.BusinessGoalNsql;
import com.daimler.data.db.entities.lov.CategoryNsql;
import com.daimler.data.db.entities.lov.MaturityLevelNsql;
import com.daimler.data.db.entities.lov.StrategicRelevanceNsql;
import com.daimler.data.db.entities.lov.DataStrategyDomainNsql;
import com.daimler.data.dto.lov.AdditionalResourceVO;
import com.daimler.data.dto.lov.BenefitRelevanceVO;
import com.daimler.data.dto.lov.BusinessGoalVO;
import com.daimler.data.dto.lov.CategoryVO;
import com.daimler.data.dto.lov.DataStrategyDomainVO;
import com.daimler.data.dto.lov.MaturityLevelVO;
import com.daimler.data.dto.lov.StrategicRelevanceVO;

@Component
public class LovAssembler {

	public BusinessGoalVO toBusinessGoalVo(BusinessGoalNsql entity) {
		BusinessGoalVO businessGoalVO = null;
		if (Objects.nonNull(entity)) {
			businessGoalVO = new BusinessGoalVO();
			businessGoalVO.setId(entity.getId());
			if (entity.getData() != null)
				businessGoalVO.setName(entity.getData().getName());
		}
		return businessGoalVO;
	}

	public List<BusinessGoalVO> toBusinessGoalVoList(List<BusinessGoalNsql> entityList) {
		List<BusinessGoalVO> businessGoalVOList = null;
		if (entityList != null && !entityList.isEmpty())
			businessGoalVOList = entityList.stream().map(n -> toBusinessGoalVo(n)).collect(Collectors.toList());
		return businessGoalVOList;
	}

	/**
	 * toStrategicRelevanceVO
	 * 
	 * @param entity
	 * @return
	 */
	public StrategicRelevanceVO toStrategicRelevanceVO(StrategicRelevanceNsql entity) {
		StrategicRelevanceVO strategicRelevanceVO = null;
		if (Objects.nonNull(entity)) {
			strategicRelevanceVO = new StrategicRelevanceVO();
			strategicRelevanceVO.setId(entity.getId());
			if (entity.getData() != null)
				strategicRelevanceVO.setName(entity.getData().getName());
		}
		return strategicRelevanceVO;
	}

	/**
	 * toStrategicRelevanceVOList
	 * 
	 * @param entityList
	 * @return
	 */
	public List<StrategicRelevanceVO> toStrategicRelevanceVOList(List<StrategicRelevanceNsql> entityList) {
		List<StrategicRelevanceVO> strategicRelevanceVOList = null;
		if (entityList != null && !entityList.isEmpty())
			strategicRelevanceVOList = entityList.stream().map(n -> toStrategicRelevanceVO(n))
					.collect(Collectors.toList());
		return strategicRelevanceVOList;
	}

	/**
	 * toBenefitRelevanceVO
	 * 
	 * @param entity
	 * @return
	 */
	public BenefitRelevanceVO toBenefitRelevanceVO(BenefitRelevanceNsql entity) {
		BenefitRelevanceVO benefitRelevanceVO = null;
		if (Objects.nonNull(entity)) {
			benefitRelevanceVO = new BenefitRelevanceVO();
			benefitRelevanceVO.setId(entity.getId());
			if (entity.getData() != null)
				benefitRelevanceVO.setName(entity.getData().getName());
		}
		return benefitRelevanceVO;
	}

	/**
	 * toBenefitRelevanceVOList
	 * 
	 * @param entityList
	 * @return
	 */
	public List<BenefitRelevanceVO> toBenefitRelevanceVOList(List<BenefitRelevanceNsql> entityList) {
		List<BenefitRelevanceVO> benefitRelevanceVOList = null;
		if (entityList != null && !entityList.isEmpty())
			benefitRelevanceVOList = entityList.stream().map(n -> toBenefitRelevanceVO(n)).collect(Collectors.toList());
		return benefitRelevanceVOList;
	}

	/**
	 * toMaturityLevelVO
	 * 
	 * @param entity
	 * @return
	 */
	public MaturityLevelVO toMaturityLevelVO(MaturityLevelNsql entity) {
		MaturityLevelVO maturityLevelVO = null;
		if (Objects.nonNull(entity)) {
			maturityLevelVO = new MaturityLevelVO();
			maturityLevelVO.setId(entity.getId());
			if (entity.getData() != null)
				maturityLevelVO.setName(entity.getData().getName());
		}
		return maturityLevelVO;
	}

	/**
	 * toMaturityLevelVOList
	 * 
	 * @param entityList
	 * @return
	 */
	public List<MaturityLevelVO> toMaturityLevelVOList(List<MaturityLevelNsql> entityList) {
		List<MaturityLevelVO> maturityLevelVOList = null;
		if (entityList != null && !entityList.isEmpty())
			maturityLevelVOList = entityList.stream().map(n -> toMaturityLevelVO(n)).collect(Collectors.toList());
		return maturityLevelVOList;
	}

	/**
	 * toCategoryVO
	 * 
	 * @param entity
	 * @return
	 */
	public CategoryVO toCategoryVO(CategoryNsql entity) {
		CategoryVO categoryVO = null;
		if (Objects.nonNull(entity)) {
			categoryVO = new CategoryVO();
			categoryVO.setId(entity.getId());
			if (entity.getData() != null)
				categoryVO.setName(entity.getData().getName());
		}
		return categoryVO;
	}

	/**
	 * toCategoryVOList
	 * 
	 * @param entityList
	 * @return
	 */
	public List<CategoryVO> toCategoryVOList(List<CategoryNsql> entityList) {
		List<CategoryVO> categoryVOList = null;
		if (entityList != null && !entityList.isEmpty())
			categoryVOList = entityList.stream().map(n -> toCategoryVO(n)).collect(Collectors.toList());
		return categoryVOList;
	}
	
	/**
	 * To convert StrategyDomainNsql to DataStrategyDomainVO
	 * 
	 * @param entityList {List<StrategyDomainNsql>}
	 * @return strategyDomainsVO {List<DataStrategyDomainVO>}
	 */
	public List<DataStrategyDomainVO> toStrategyDomainVOList(List<DataStrategyDomainNsql> entityList) {
		List<DataStrategyDomainVO> strategyDomainsVOList = null;
		if (entityList != null && !entityList.isEmpty())
			strategyDomainsVOList = entityList.stream().map(n -> toStrategyDomainVO(n)).collect(Collectors.toList());
		return strategyDomainsVOList;
	}

	/**
	 * To convert StrategyDomainNsql to DataStrategyDomainVO
	 * 
	 * @param entity {StrategyDomainNsql}
	 * @return strategyDomains {DataStrategyDomainVO}
	 */
	public DataStrategyDomainVO toStrategyDomainVO(DataStrategyDomainNsql entity) {
		DataStrategyDomainVO dataStrategyDomainVO = null;
		if (Objects.nonNull(entity)) {
			dataStrategyDomainVO = new DataStrategyDomainVO();
			dataStrategyDomainVO.setId(entity.getId());
			if (entity.getData() != null)
				dataStrategyDomainVO.setName(entity.getData().getName());
		}
		return dataStrategyDomainVO;
	}
	
	/**
	 * To convert AdditionalResourceNsql to AdditionalResourceVO
	 * 
	 * @param entityList {List<AdditionalResourceNsql>}
	 * @return additionalResourcesVO {List<AdditionalResourceVO>}
	 */
	public List<AdditionalResourceVO> toAdditionalResourceVOList(List<AdditionalResourceNsql> entityList) {
		List<AdditionalResourceVO> additionalResourcesVO = null;
		if (!ObjectUtils.isEmpty(entityList)) {
			additionalResourcesVO = entityList.stream().map(n -> toAdditionalResourceVO(n))
					.collect(Collectors.toList());
		}

		return additionalResourcesVO;
	}

	/**
	 * To convert AdditionalResourceNsql to AdditionalResourceVO
	 * 
	 * @param entity {AdditionalResourceNsql}
	 * @return additionalResourceVO {AdditionalResourceVO}
	 */
	public AdditionalResourceVO toAdditionalResourceVO(AdditionalResourceNsql entity) {
		AdditionalResourceVO additionalResourceVO = null;
		if (Objects.nonNull(entity)) {
			additionalResourceVO = new AdditionalResourceVO();
			additionalResourceVO.setId(entity.getId());
			if (entity.getData() != null) {
				additionalResourceVO.setName(entity.getData().getName());
			}
		}
		return additionalResourceVO;
	}
}
