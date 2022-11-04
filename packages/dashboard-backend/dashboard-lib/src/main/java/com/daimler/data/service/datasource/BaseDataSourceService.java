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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.dataSource.DataSourceRequestVO;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.report.DataSourceVO;
import com.daimler.data.service.report.ReportService;

@Service
public class BaseDataSourceService implements DataSourceService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataSourceService.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserStore userStore;

	public BaseDataSourceService() {
		super();
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> updateDataSource(DataSourceRequestVO dataSourceRequestVO) {
		DataSourceVO vo = dataSourceRequestVO.getData();
		String name = vo.getDataSource();
		try {
			if (!isSuperAdmin()) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to update dataSource for existing reports. User does not have admin privileges.");
				LOGGER.debug("DataSource {} cannot be updated for existing reports. User not authorized", name);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			LOGGER.debug("Calling reportService updateForEachReport to update cascading refences to dataSource {}",
					name);
			reportService.updateForEachReport(name, "", ReportService.CATEGORY.DATASOURCE, vo);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("DataSource {} updated successfully for existing reports", name);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Failed while updating dataSource {} from existing reports with exception {}", name,
					e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to update due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataSource(String name) {
		try {
			if (!isSuperAdmin()) {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete dataSource for existing reports. User does not have admin privileges.");
				LOGGER.debug("DataSource {} cannot be deleted for existing reports. User not authorized", name);
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
			LOGGER.debug("Calling reportService deleteForEachReport to delete cascading refences to dataSource {}",
					name);
			reportService.deleteForEachReport(name, ReportService.CATEGORY.DATASOURCE);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			LOGGER.info("DataSource {} deleted successfully for existing reports", name);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Failed while deleting dataSource {} for existing reports with exception {}", name,
					e.getMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private boolean isSuperAdmin() {
		Boolean isAdmin = false;
		CreatedByVO currentUser = this.userStore.getVO();
		String userId = currentUser != null ? currentUser.getId() : null;
		if (StringUtils.hasText(userId)) {
			isAdmin = this.userStore.getUserInfo().hasSuperAdminAccess();
		}
		return isAdmin;
	}

}
