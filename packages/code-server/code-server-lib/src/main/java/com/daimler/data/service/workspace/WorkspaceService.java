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

package com.daimler.data.service.workspace;

import java.util.List;

import javax.validation.Valid;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.dto.workspace.*;
import com.daimler.data.dto.workspace.admin.CodespaceSecurityConfigDetailsVO;
import com.daimler.data.dto.workspace.buildDeploy.ManageBuildRequestDto;


public interface WorkspaceService {

	GenericMessage deleteById(String userId,String id);

	CodeServerWorkspaceVO getById(String userId, String id);
	
	List<CodeServerWorkspaceVO> getAll(String userId,int offset,int limit);
	
	CodeServerWorkspaceVO getByUniqueliteral(String userId, String uniqueLiteral, String value);

	Integer getCount(String userId);

	CodeSpaceReadmeVo getCodeSpaceReadmeFile(String id) throws Exception;

	InitializeWorkspaceResponseVO createWorkspace(CodeServerWorkspaceVO vo, String pat);

	InitializeWorkspaceResponseVO initiateWorkspace(CodeServerWorkspaceVO vo, String pat);

	InitializeWorkspaceResponseVO initiateWorkspacewithAdminPat(CodeServerWorkspaceVO vo, String pat);

	CodeServerWorkspaceVO getByProjectName(String userId, String projectName);

	GenericMessage update(String userId, String name, String projectName, String existingStatus, String latestStatus, String targetEnv, String branch, String gitJobRunId);

	GenericMessage approveRequestWorkspace(String userId, String id, String environment, String branch, boolean isSecureWithIAMRequired, 
		String clientID, String clientSecret, String redirectUri, String ignorePaths, String scope,boolean isApiRecipe,String oneApiVersionShortName, boolean isSecuredWithCookie, boolean isprivateRecipe);

	GenericMessage deployWorkspace(String userId, String id, String environment, String branch, boolean isSecureWithIAMRequired, 
		String clientID, String clientSecret, String redirectUri, String ignorePaths, String scope,boolean isApiRecipe,String oneApiVersionShortName, boolean isSecuredWithCookie, boolean isprivateRecipe);

	GenericMessage undeployWorkspace(String userId, String id, String environment, String branch);

	GenericMessage addCollabById(String userId,  CodeServerWorkspaceVO vo, UserInfoVO userRequestDto);

	GenericMessage removeCollabById(String currentUserUserId,  CodeServerWorkspaceVO vo, String userRequestDto);

	GenericMessage reassignOwner(CreatedByVO currentUser, CodeServerWorkspaceVO vo, UserInfoVO newOwnerDeatils);

	Integer getTotalCountOfWorkSpace();

	List<String> getAllWorkspaceIds();

	CodeServerWorkspaceValidateVO validateCodespace(String id, String userId);

	GenericMessage saveSecurityConfig(CodeServerWorkspaceVO vo, Boolean isPublished, String env);

	GenericMessage makeAdmin(CodeServerWorkspaceVO vo);

	GenericMessage makeApprover(CodeServerWorkspaceVO vo);

    List<CodespaceSecurityConfigDetailsVO> getAllSecurityConfigs(Integer offset, Integer limit, String projectName);

	//GenericMessage updateSecurityConfigStatus(String projectName, String Status, String user, CodeServerWorkspaceVO vo);

	GenericMessage updateGovernancenceValues(String userId, String id,
			@Valid DataGovernanceRequestInfo dataGovernanceInfo);

	CodeServerWorkspaceVO getByProjectName(String projectName);

    String getServerStatus(CodeServerWorkspaceVO vo);

	GenericMessage startServer(String userId,String wsId, String cloudServiceProvider);

    GenericMessage stopServer(CodeServerWorkspaceVO vo, String cloudServiceProvider);

    GenericMessage moveExistingWorkspace(CodeServerWorkspaceNsql vo);

	GenericMessage updateResourceValue(CodeServerWorkspaceNsql entity, @Valid ResourceVO updatedResourceValue);

	GenericMessage restartWorkspace(String userId, String id, String env);

	GenericMessage migrateWorkspace(CodeServerWorkspaceNsql entity);

	GenericMessage buildWorkSpace(String userId,String id,String branch,ManageBuildRequestDto buildRequestDto,boolean isPrivateRecipe,String environment);

	CodeServerWorkspaceVO findByWorkspaceId(String wsId);
	
}
