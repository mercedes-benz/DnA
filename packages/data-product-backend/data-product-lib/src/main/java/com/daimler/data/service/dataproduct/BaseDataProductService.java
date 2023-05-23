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

package com.daimler.data.service.dataproduct;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.dataproduct.DataProductTeamMemberVO;
import com.daimler.data.dto.datatransfer.ConsumerVO;
import com.daimler.data.dto.datatransfer.DataTransferConsumerResponseVO;
import com.daimler.data.dto.datatransfer.DataTransferProviderResponseVO;
import com.daimler.data.dto.datatransfer.ProviderVO;
import com.daimler.data.service.datatransfer.DataTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.repo.dataproduct.DataProductCustomRepository;
import com.daimler.data.db.repo.dataproduct.DataProductRepository;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.notifications.common.producer.KafkaProducerService;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;

@Service
public class BaseDataProductService extends BaseCommonService<DataProductVO, DataProductNsql, String>
		implements DataProductService {
		
		private static Logger LOGGER = LoggerFactory.getLogger(BaseDataProductService.class);
		
		@Value(value = "${dataproduct.base.url}")
		private String dataProductBaseUrl;
		
		@Autowired
		private UserStore userStore;
		
		@Autowired
		private DataProductAssembler dataProductAssembler;
		
		@Autowired
		private DepartmentService departmentService;
		
		@Autowired
		private KafkaProducerService kafkaProducer;
		
		@Autowired
		private DataProductCustomRepository dataProductCustomRepository;
		
		@Autowired
		private DataProductRepository dataProductRepository;

		@Autowired
		private DataTransferService dataTransferService;
		
		public BaseDataProductService() {
			super();
		}

		@Override
		public List<DataProductVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy,
				String sortOrder, String recordStatus, List<String> artsList,
				List<String> carlafunctionsList, List<String> platformsList,
				List<String> frontendToolsList, List<String> productOwnerList) {
			List<DataProductNsql> dataProductEntities = dataProductCustomRepository
					.getAllWithFiltersUsingNativeQuery(published, offset, limit, sortBy, sortOrder, recordStatus,
							artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
			if (!ObjectUtils.isEmpty(dataProductEntities))
				return dataProductEntities.stream().map(n -> dataProductAssembler.toVo(n)).collect(Collectors.toList());
			else
				return new ArrayList<>();
		}

	@Override
	public List<DataProductTeamMemberVO> getAllWithDataProductOwners(Boolean published, int offset, int limit, String sortOrder,
																	 String recordStatus) {
		List<DataProductTeamMemberVO> dataProductEntities = dataProductCustomRepository
				.getOwnersAllWithFiltersUsingNativeQuery(published, offset, limit, sortOrder, recordStatus);
		return dataProductEntities;
	}

	@Override
	public ResponseEntity<DataTransferProviderResponseVO> createDataTransferProvider(ProviderVO providerVO) {
		return dataTransferService.createDataTransferProvider(providerVO, true);
	}

	@Override
	public ResponseEntity<DataTransferConsumerResponseVO> updateDataTransferConsumer(ConsumerVO consumerVO) {
		return dataTransferService.updateDataTransferConsumer(consumerVO, true);
	}

	@Override
		public Long getCount(Boolean published, String recordStatus,
			List<String> artsList, List<String> carlafunctionsList,
			List<String> platformsList, List<String> frontendToolsList,
			List<String> productOwnerList) {
			return dataProductCustomRepository.getCountUsingNativeQuery(published, recordStatus,
					artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
		}

		@Override
		public Long getCountOwners(Boolean published, String recordStatus) {
			return dataProductCustomRepository.getCountOwnersUsingNativeQuery(published, recordStatus);
		}

		@Override
		public List<DataProductVO> getExistingDataProduct(String uniqueProductName, String status) {
			LOGGER.info("Fetching Data product information from table for getExistingDataProduct.");
			List<DataProductNsql> dataProductNsqls = dataProductCustomRepository.getExistingDataProduct(uniqueProductName, status);
			LOGGER.info("Success from get information from table.");
			if (!ObjectUtils.isEmpty(dataProductNsqls)) {
				return dataProductNsqls.stream().map(n -> dataProductAssembler.toVo(n)).toList();
			} else {
				return new ArrayList<>();
			}	
		}

	@Override
	public GenericMessage updateDataProductData() {
		return dataProductCustomRepository.updateDataProductData();
	}

	@Override
		@Transactional
		public String getNextSeqId() {
			return String.format("%05d",dataProductRepository.getNextSeqId());
		}


}
