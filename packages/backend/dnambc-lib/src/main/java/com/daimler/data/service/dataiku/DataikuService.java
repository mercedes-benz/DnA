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

package com.daimler.data.service.dataiku;

import java.util.List;

import com.daimler.data.dto.dataiku.DataikuProjectVO;

public interface DataikuService {

	/**
	 * <p>
	 * getAll DataikuProjects identified for user.
	 * </p>
	 * 
	 * @param userId
	 * @param isAdmin
	 * @param environment
	 */
	public List<DataikuProjectVO> getAllDataikuProjects(String userId, Boolean live, String cloudProfile);

	/**
	 * get Dataiku project by given projectKey
	 * 
	 * @param projectKey
	 * @return DataikuProjectVO
	 */
	public DataikuProjectVO getByProjectKey(String projectKey, Boolean live, String cloudProfile);

	/**
	 * map dataiku project with solution
	 * 
	 * @param dataikuProjectId
	 * @param solutionId
	 */
	public void updateSolutionIdOfDataIkuProjectId(String dataikuProjectId, String solutionId);
}
