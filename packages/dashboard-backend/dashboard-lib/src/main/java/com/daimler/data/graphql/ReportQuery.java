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

package com.daimler.data.graphql;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.daimler.data.controller.ReportController;
import com.daimler.data.dto.report.ReportCollection;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.service.report.ReportService;

@Component
public class ReportQuery implements GraphQLQueryResolver {

	@Autowired
	private ReportService reportService;

//    @Autowired
//    private UserInfoService userInfoService;

	@Autowired
	private ReportController reportController;

	public Optional<ReportVO> getReport(String id) {
		return Optional.of(this.reportService.getById(id));

	}

	public ReportCollection getReports(Boolean published, String status, String searchTerm, String tags, int offset,
			int limit, String sortBy, String sortOrder, String division, String department, String processOwner,
			String art) {

		ResponseEntity<ReportCollection> reports = reportController.getAll(published, status, searchTerm, tags, offset,
				limit, sortBy, sortOrder, division, department, processOwner, art);

		if (reports != null && reports.getBody() != null) {
			return reports.getBody();
		} else {
			return null;
		}

	}

//    public ReportCollection getBookmarkedReports(String userId){
//        List<ReportVO> reportVOList  = userInfoService.getAllBookMarkedReportsForUser(userId);
//        ReportCollection response = new ReportCollection();
//        response.setTotalCount(reportVOList!=null?reportVOList.size():0);
//        response.setRecords(reportVOList);
//        return response;
//    }

}
