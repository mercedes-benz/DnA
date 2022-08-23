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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.daimler.data.controller.SolutionController;
import com.daimler.data.dto.solution.SolutionCollection;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.service.solution.SolutionService;
import com.daimler.data.service.userinfo.UserInfoService;

@Component
public class SolutionQuery implements GraphQLQueryResolver {

	@Autowired
	private SolutionService solutionService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private SolutionController solutionController;

	public Optional<SolutionVO> getSolution(String id) {
		return Optional.of(this.solutionService.getById(id));

	}

	public SolutionCollection getSolutions(Boolean published, String division, String location, String phase,
			String dataVolume, String projectstatus, String useCaseType, String searchTerm, String tags,
			Boolean hasDigitalValue, Boolean hasNotebook, int offset, int limit, String sortBy, String sortOrder) {

		ResponseEntity<SolutionCollection> solutions = solutionController.getAll(published, location, division, phase,
				dataVolume, projectstatus, useCaseType, searchTerm, tags, hasDigitalValue, hasNotebook, offset, limit,
				sortBy, sortOrder);

		if (solutions != null && solutions.getBody() != null) {
			return solutions.getBody();
		} else {
			return null;
		}

	}

	public SolutionCollection getBookmarkedSolutions(String userId) {
		List<SolutionVO> solutionVOList = userInfoService.getAllBookMarkedSolutionsForUser(userId);
		SolutionCollection response = new SolutionCollection();
		response.setTotalCount(solutionVOList != null ? solutionVOList.size() : 0);
		response.setRecords(solutionVOList);
		return response;
	}

}
