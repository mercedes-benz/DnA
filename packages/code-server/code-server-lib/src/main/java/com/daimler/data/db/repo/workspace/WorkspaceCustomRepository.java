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

package com.daimler.data.db.repo.workspace;

import java.util.List;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerDeploymentDetails;
import com.daimler.data.db.json.UserInfo;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface WorkspaceCustomRepository extends CommonDataRepository<CodeServerWorkspaceNsql, String> {

	List<CodeServerWorkspaceNsql> findAll(String userId, int limit, int offset);

	Integer getCount(String userId);

	CodeServerWorkspaceNsql findbyUniqueLiteral(String userId, String uniqueLiteral, String value);

	CodeServerWorkspaceNsql findById(String userId, String id);

	CodeServerWorkspaceNsql findbyProjectName(String userId, String projectName);

	GenericMessage updateDeploymentDetails(String projectName, String environment,
			CodeServerDeploymentDetails deploymentDetails);

	GenericMessage updateProjectOwnerDetails(String projectName, UserInfo updatedProjectOwnerDetails);

	GenericMessage updateCollaboratorDetails(String projectName, UserInfo updatedcollaborators, boolean removeUser);

	List<Object[]> getWorkspaceIdsForProjectMembers(String projectName);

	String getWorkspaceTechnicalId(String userId, String projectName);

	void updateDeletedStatusForProject(String projectName);

	Integer getTotalCountOfWorkSpace(String status);

}
