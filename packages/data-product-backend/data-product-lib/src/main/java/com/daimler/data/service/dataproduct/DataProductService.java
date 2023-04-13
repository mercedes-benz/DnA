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

import java.util.List;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.datatransfer.ConsumerVO;
import com.daimler.data.dto.datatransfer.DataTransferConsumerResponseVO;
import com.daimler.data.dto.datatransfer.DataTransferProviderResponseVO;
import com.daimler.data.dto.datatransfer.ProviderVO;
import com.daimler.data.service.common.CommonService;
import org.springframework.http.ResponseEntity;

public interface DataProductService extends CommonService<DataProductVO, DataProductNsql, String> {

	List<DataProductVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy, String sortOrder,
			String recordStatus, List<String> artsList, List<String> carlafunctionsList,
			List<String> platformsList, List<String> frontendToolsList, List<String> productOwnerList);

	List<String> getAllWithDataProductOwners(Boolean published, int offset, int limit, String sortOrder,
			String recordStatus);

	ResponseEntity<DataTransferProviderResponseVO> createDataTransferProvider(ProviderVO providerVO);

	ResponseEntity<DataTransferConsumerResponseVO> updateDataTransferConsumer(ConsumerVO consumerVO);

	Long getCount(Boolean published, String recordStatus,
				  List<String> artsList, List<String> carlafunctionsList,
				  List<String> platformsList, List<String> frontendToolsList, List<String> productOwnerList);

	List<DataProductVO> getExistingDataProduct(String uniqueProductName, String status);

	GenericMessage updateDataProductData();

	Long getCountOwners(Boolean published, String recordStatus);

	String getNextSeqId();
}
