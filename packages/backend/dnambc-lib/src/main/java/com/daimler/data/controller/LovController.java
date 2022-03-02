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

package com.daimler.data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.lov.LovApi;
import com.daimler.data.dto.lov.BenefitRelevanceVO;
import com.daimler.data.dto.lov.BenefitRelevanceVOCollection;
import com.daimler.data.dto.lov.BusinessGoalVO;
import com.daimler.data.dto.lov.BusinessGoalVOCollection;
import com.daimler.data.dto.lov.CategoryVO;
import com.daimler.data.dto.lov.CategoryVOCollection;
import com.daimler.data.dto.lov.DataStrategyDomainVO;
import com.daimler.data.dto.lov.DataStrategyDomainVOCollection;
import com.daimler.data.dto.lov.MaturityLevelVO;
import com.daimler.data.dto.lov.MaturityLevelVOCollection;
import com.daimler.data.dto.lov.StrategicRelevanceVO;
import com.daimler.data.dto.lov.StrategicRelevanceVOCollection;
import com.daimler.data.service.lov.LovService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Lov API", tags = { "lov" })
@RequestMapping("/api/lov")
@Slf4j
public class LovController implements LovApi {

	@Autowired
	private LovService lovService;

	/**
	 * <p>
	 * getAllBusinessGoal
	 * <P>
	 * Get all business Goals
	 * 
	 * @return BusinessGoalVOCollection
	 * 
	 */
	@Override
	@ApiOperation(value = "Get all business Goals.", nickname = "getAllBusinessGoal", notes = "Get all business Goals. This endpoints will be used to Get all valid available business Goals.", response = BusinessGoalVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = BusinessGoalVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/businessgoals", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "business-goals")
	public ResponseEntity<BusinessGoalVOCollection> getAllBusinessGoal() {
		final List<BusinessGoalVO> businessGoalsVO = lovService.getAllBusinessGoal();
		BusinessGoalVOCollection businessGoalVOCollection = new BusinessGoalVOCollection();
		if (businessGoalsVO != null && businessGoalsVO.size() > 0) {
			businessGoalVOCollection.setData(businessGoalsVO);
			log.debug("Returning all available business goals");
			return new ResponseEntity<>(businessGoalVOCollection, HttpStatus.OK);
		} else {
			log.debug("No business goals available, returning empty");
			return new ResponseEntity<>(businessGoalVOCollection, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * getAllBenefitRelevance
	 * <P>
	 * get All Benefit Relevance
	 * 
	 * @return BenefitRelevanceVOCollection
	 * 
	 */
	@Override
	@ApiOperation(value = "Get all benefit relevances.", nickname = "getAllBenefitRelevance", notes = "Get all benefit relevances. This endpoints will be used to Get all valid available benefit relevances.", response = BenefitRelevanceVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = BenefitRelevanceVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/benefitrelevances", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "benifit-relevance")
	public ResponseEntity<BenefitRelevanceVOCollection> getAllBenefitRelevance() {
		final List<BenefitRelevanceVO> benefitRelevancesVO = lovService.getAllBenefitRelevance();
		BenefitRelevanceVOCollection benefitRelevanceVOCollection = new BenefitRelevanceVOCollection();
		if (benefitRelevancesVO != null && benefitRelevancesVO.size() > 0) {
			benefitRelevanceVOCollection.setData(benefitRelevancesVO);
			log.debug("Returning all available benefit relevance");
			return new ResponseEntity<>(benefitRelevanceVOCollection, HttpStatus.OK);
		} else {
			log.debug("No benefit relevance collection found, returning empty");
			return new ResponseEntity<>(benefitRelevanceVOCollection, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * getAllMaturityLevel
	 * <P>
	 * get All Maturity Level
	 * 
	 * @return MaturityLevelVOCollection
	 * 
	 */
	@Override
	@ApiOperation(value = "Get all Maturity levels.", nickname = "getAllMaturityLevel", notes = "Get all Maturity levels. This endpoints will be used to Get all valid available Maturity levels.", response = MaturityLevelVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = MaturityLevelVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/maturitylevels", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "maturity-level")
	public ResponseEntity<MaturityLevelVOCollection> getAllMaturityLevel() {
		final List<MaturityLevelVO> maturityLevelsVO = lovService.getAllMaturityLevel();
		MaturityLevelVOCollection maturityLevelVOCollection = new MaturityLevelVOCollection();
		if (maturityLevelsVO != null && maturityLevelsVO.size() > 0) {
			maturityLevelVOCollection.setData(maturityLevelsVO);
			log.debug("Returning all available maturity levels");
			return new ResponseEntity<>(maturityLevelVOCollection, HttpStatus.OK);
		} else {
			log.debug("No Maturity levels found, returning empty");
			return new ResponseEntity<>(maturityLevelVOCollection, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * getAllStrategicRelevance
	 * <P>
	 * Get all strategic relevances
	 * 
	 * @return StrategicRelevanceVOCollection
	 * 
	 */
	@Override
	@ApiOperation(value = "Get all strategic relevances.", nickname = "getAllStrategicRelevance", notes = "Get all strategic relevances. This endpoints will be used to Get all valid available strategic relevances.", response = StrategicRelevanceVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = StrategicRelevanceVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/strategicrelevances", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "strategy-relevance")
	public ResponseEntity<StrategicRelevanceVOCollection> getAllStrategicRelevance() {
		final List<StrategicRelevanceVO> strategicRelevancesVO = lovService.getAllStrategicRelevance();
		StrategicRelevanceVOCollection strategicRelevanceVOCollection = new StrategicRelevanceVOCollection();
		if (strategicRelevancesVO != null && strategicRelevancesVO.size() > 0) {
			strategicRelevanceVOCollection.setData(strategicRelevancesVO);
			log.debug("Returning all available strategic relevance collection");
			return new ResponseEntity<>(strategicRelevanceVOCollection, HttpStatus.OK);
		} else {
			log.debug("No strategic relevance collection found, returning empty");
			return new ResponseEntity<>(strategicRelevanceVOCollection, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * getAllCategory
	 * <P>
	 * Get all categories
	 * 
	 * @return CategoryVOCollection
	 * 
	 */
	@Override
	@ApiOperation(value = "Get all categories.", nickname = "getAllCategory", notes = "Get all categories. This endpoints will be used to Get all valid available categories.", response = CategoryVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = CategoryVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/categories", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "all-category")
	public ResponseEntity<CategoryVOCollection> getAllCategory() {
		final List<CategoryVO> categoriesVO = lovService.getAllCategory();
		CategoryVOCollection categoryVOCollection = new CategoryVOCollection();
		if (categoriesVO != null && categoriesVO.size() > 0) {
			categoryVOCollection.setData(categoriesVO);
			log.debug("Returning all available categories");
			return new ResponseEntity<>(categoryVOCollection, HttpStatus.OK);
		} else {
			log.debug("No categories collection found, returning empty");
			return new ResponseEntity<>(categoryVOCollection, HttpStatus.NO_CONTENT);
		}
	}

	@Override
	@ApiOperation(value = "Get all data domain strategy.", nickname = "getAllStrategyDomain", notes = "Get all data domain strategy. This endpoints will be used to Get all valid available data domain strategy.", response = DataStrategyDomainVOCollection.class, tags = {
			"lov", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = DataStrategyDomainVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/strategydomains", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@Cacheable(value = "all-dataStrategyDomain")
	public ResponseEntity<DataStrategyDomainVOCollection> getAllStrategyDomain() {
		final List<DataStrategyDomainVO> strategyDomainsVO = lovService.getAllStrategyDomain();
		DataStrategyDomainVOCollection strategyDomainVOCollection = new DataStrategyDomainVOCollection();
		if (!ObjectUtils.isEmpty(strategyDomainsVO)) {
			strategyDomainVOCollection.setData(strategyDomainsVO);
			return new ResponseEntity<>(strategyDomainVOCollection, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(strategyDomainVOCollection, HttpStatus.NO_CONTENT);
		}
	}

}
