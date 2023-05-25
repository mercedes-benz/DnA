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

package com.daimler.data.service.report;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.dto.report.ProcessOwnerCollection;
import com.daimler.data.dto.report.ReportResponseVO;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.service.common.CommonService;

public interface ReportService extends CommonService<ReportVO, ReportNsql, String> {

	enum CATEGORY {
		TAG, DEPARTMENT, INTEGRATED_PORTAL, FRONTEND_TECH, ART, STATUS, DATA_WAREHOUSE,
		CUST_DEPARTMENT, LEVEL, LEGAL_ENTITY, KPI_NAME, REPORTING_CAUSE, DATASOURCE, DATA_CLASSIFICATION,
		CONNECTION_TYPE, DIVISION;
	}

	/**
	 * To getAll Reports with given filters.
	 * 
	 * @param published
	 * @param statuses
	 * @param userId
	 * @param isAdmin
	 * @param searchTerms
	 * @param tags
	 * @param offset
	 * @param limit
	 * @param sortBy
	 * @param sortOrder
	 * @return reports{List<ReportVO>}
	 */
	List<ReportVO> getAllWithFilters(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy, String sortOrder,
			String division, List<String> department, List<String> processOwner, List<String> art);

	/**
	 * To get Count of all the reports with given filters.
	 * 
	 * @param published
	 * @param statuses
	 * @param userId
	 * @param isAdmin
	 * @param searchTerms
	 * @param tags
	 * @return count{Long}
	 */
	Long getCount(Boolean published, List<String> statuses, String userId, Boolean isAdmin, List<String> searchTerms,
			List<String> tags, String division, List<String> department, List<String> processOwner, List<String> art);

	/**
	 * To delete for each report.
	 * 
	 * @param name
	 * @param category
	 */
	void deleteForEachReport(String name, CATEGORY category);

	/**
	 * To create a new Report.
	 * 
	 * @param requestReportVO
	 * @return ResponseEntity<ReportResponseVO>
	 */
	ResponseEntity<ReportResponseVO> createReport(ReportVO requestReportVO);

	/**
	 * To update an existing Report.
	 * 
	 * @param requestReportVO
	 * @return ResponseEntity<ReportResponseVO>
	 */
	ResponseEntity<ReportResponseVO> updateReport(ReportVO requestReportVO);

	/**
	 * To delete a report.
	 * 
	 * @param id
	 * @return ResponseEntity<GenericMessage>
	 */
	ResponseEntity<GenericMessage> deleteReport(String id);

	ResponseEntity<ProcessOwnerCollection> getProcessOwners();

	/**
	 * To update for each report.
	 * 
	 * @param name
	 * @param category
	 */
	void updateForEachReport(String oldValue, String newValue, CATEGORY category, Object updateObject);

	Integer getCountBasedPublishReport(Boolean published);
}
