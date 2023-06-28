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

package com.daimler.data.service.datatransfer;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.DataTransferNsql;
import com.daimler.data.dto.datatransfer.*;
import com.daimler.data.service.common.CommonService;

public interface DataTransferService extends CommonService<DataTransferVO, DataTransferNsql, String> {

	enum CATEGORY {

	}

	List<DataTransferVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy, String sortOrder,
			String recordStatus, String datatransferIds, Boolean isCreator, Boolean isProviderCreator);

	Long getCount(Boolean published, String recordStatus, String datatransferIds, Boolean isCreator, Boolean isProviderCreator);

	ResponseEntity<DataTransferProviderResponseVO> createDataTransferProvider(ProviderVO providerVO, Boolean isDataProductService);

	ResponseEntity<DataTransferProviderResponseVO> updateDataTransferProvider(ProviderVO providerVO);

	ResponseEntity<DataTransferConsumerResponseVO> updateDataTransferConsumer(ConsumerVO consumerVO, Boolean isDataProductService);

	ResponseEntity<GenericMessage> deleteDataTransfer(String id);

	GenericMessage updateDataTransferData();

	Integer getCountBasedPublishDatatransfer(Boolean published);
}
