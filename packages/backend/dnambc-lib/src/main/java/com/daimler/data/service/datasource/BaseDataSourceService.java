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

package com.daimler.data.service.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.assembler.DataSourceAssembler;
import com.daimler.data.client.dashboard.DashboardClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataSourceNsql;
import com.daimler.data.db.repo.datasource.DataSourceCustomRepository;
import com.daimler.data.db.repo.datasource.DataSourceRepository;
import com.daimler.data.dto.datasource.DataSourceCreateVO;
import com.daimler.data.dto.datasource.DataSourceVO;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.util.ConstantsUtility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseDataSourceService extends BaseCommonService<DataSourceVO, DataSourceNsql, String>
		implements DataSourceService {

	private static Logger logger = LoggerFactory.getLogger(BaseDataSourceService.class);
	
	@Autowired
	private DataSourceCustomRepository customRepo;
	@Autowired
	private DataSourceRepository jpaRepo;
	@Autowired
	private DataSourceAssembler assembler;

	@Autowired
	private SolutionService solutionService;
	
	@Autowired
	private DashboardClient dashboardClient;

	@Value("${dna.dataSource.bulkCreate.api.accessToken}")
	private String accessToken;
	
	public BaseDataSourceService() {
		super();
	}

	@Transactional
	@Override
	public void deleteDataSource(final String id) {
		DataSourceNsql dsEntity = jpaRepo.getOne(id);
		String dsName = dsEntity.getData().getName();
		log.debug("Calling solutionService deleteTagForEachSolution to delete cascading refences to datasource {}", id);
		solutionService.deleteTagForEachSolution(dsName, null, SolutionService.TAG_CATEGORY.DS);
		deleteById(id);
		log.debug("Calling dashboardService to delete cascading refences to datasource {}", id);
		dashboardClient.deleteDataSourceFromEachReport(dsName);
	}

	@Transactional
	@Override
	public ResponseEntity<GenericMessage> bulkCreate(List<DataSourceCreateVO> dataSourcesCreateVO) {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		if (ObjectUtils.isEmpty(dataSourcesCreateVO)) {
			logger.info("Empty list found for bulk datasource creation.");
			genericMessage.setErrors(Arrays.asList(new MessageDescription("Empty record cannot be created.")));
			httpStatus = HttpStatus.BAD_REQUEST;
		} else {
			// Iterating over list of data-sources
			for (DataSourceCreateVO dataSourceCreateVO : dataSourcesCreateVO) {
				DataSourceNsql dataSourceNsql = customRepo.findbyUniqueLiteral("name", dataSourceCreateVO.getName());
				if (dataSourceNsql != null && dataSourceNsql.getData() != null) {
					logger.info("Datasource:{} already exists.", dataSourceCreateVO.getName());
				} else {
					logger.info("Creating new datasource:{}", dataSourceCreateVO.getName());
					DataSourceVO dataSourceVO = new DataSourceVO();
					BeanUtils.copyProperties(dataSourceCreateVO, dataSourceVO);
					super.create(dataSourceVO);
				}
			}
			httpStatus = HttpStatus.OK;
			genericMessage.setSuccess("SUCCESS");
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}
	
	@Override
	public boolean accessTokenIntrospection(String token) {
		boolean validAccessToken = false;
		if(StringUtils.hasText(token) && token.equals(accessToken)) {
			validAccessToken = true;
		}
		return validAccessToken;
	}

	@Override
	public List<DataSourceVO> getAllDataCatalogs(String source,String sortOrder) {
		// TODO Auto-generated method stub
		logger.info("Fetching data catalog information from table for getAllDataCatalogs.");
		List<DataSourceNsql> dataCatalogInfoEntities = customRepo.getAllDataCatalogs(source,sortOrder);
		logger.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(dataCatalogInfoEntities)) {
			return dataCatalogInfoEntities.stream().map(n -> assembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}
	}
	
	
}
