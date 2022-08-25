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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.application.client.CodeServerClient;
import com.daimler.data.assembler.WorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseWorkspaceService implements WorkspaceService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseWorkspaceService.class);

	@Autowired
	private WorkspaceAssembler workspaceAssembler;
	@Autowired
	private WorkspaceRepository workspaceRepository;
	@Autowired
	private WorkspaceCustomRepository workspaceCustomRepository;
	@Autowired
	private WorkspaceRepository jpaRepo;
	
	@Autowired
	private CodeServerClient client;
	
	public BaseWorkspaceService() {
		super();
	}

	@Override
	public GenericMessage deleteById(String id) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			Optional<CodeServerWorkspaceNsql> entityOptional = jpaRepo.findById(id);
			CodeServerWorkspaceNsql entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
			if(entity!=null && entity.getData()!=null && !"DELETED".equalsIgnoreCase(entity.getData().getStatus())) {
				GenericMessage undeployJobResponse = client.performWorkBenchActions("undeploy", entity.getData());
				if(undeployJobResponse!=null && "SUCCESS".equalsIgnoreCase(undeployJobResponse.getSuccess())) {
					GenericMessage deleteJobResponse = client.performWorkBenchActions("delete", entity.getData());
					if(deleteJobResponse!=null && "SUCCESS".equalsIgnoreCase(deleteJobResponse.getSuccess())) {
						entity.getData().setStatus("DELETE_REQUESTED");
						jpaRepo.save(entity);
						status = "SUCCESS";
					}else {
						errors.addAll(deleteJobResponse.getErrors());
					}
				}else {
					errors.addAll(undeployJobResponse.getErrors());
				}
			}
		}catch(Exception e) {
			LOGGER.error("Error occured while deleting workspace {} in database with exception {} ", id, e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while deleting workspace with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setSuccess(status);
		responseMessage.setWarnings(warnings);
		responseMessage.setErrors(errors);
		return responseMessage;
	}


	@Override
	public InitializeWorkspaceResponseVO create(CodeServerWorkspaceVO vo, String password) {
		InitializeWorkspaceResponseVO responseVO = new InitializeWorkspaceResponseVO();
		responseVO.setData(vo);
		responseVO.setSuccess("FAILED");
		try {
			CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(vo);
			entity.getData().setPassword(password);
			//SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			entity.getData().setIntiatedOn(new Date());
			entity.getData().setStatus("CREATE_REQUESTED");
			GenericMessage jobResponse = client.performWorkBenchActions("create", entity.getData());
			if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
				CodeServerWorkspaceNsql savedEntity = jpaRepo.save(entity);
				CodeServerWorkspaceVO savedVO = workspaceAssembler.toVo(savedEntity);
				responseVO.setData(savedVO);
				responseVO.setSuccess("SUCCESS");
				return responseVO;
			}else {
				responseVO.setErrors(jobResponse.getErrors());
				responseVO.setWarnings(jobResponse.getWarnings());
				return responseVO;
			}
		}catch(Exception e) {
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while creating codeserver workspace with exception " + e.getMessage());
			errors.add(error);
			responseVO.setErrors(errors);
			return responseVO;
		}
	}


	@Override
	public CodeServerWorkspaceVO getById(String id) {
		Optional<CodeServerWorkspaceNsql> entityOptional = jpaRepo.findById(id);
		CodeServerWorkspaceNsql entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
		return workspaceAssembler.toVo(entity);
	}


	@Override
	public List<CodeServerWorkspaceVO> getAll(String userId, int offset, int limit) {
		List<CodeServerWorkspaceNsql> entities = workspaceCustomRepository.findAll(userId,limit, offset);
		return entities.stream().map(n -> workspaceAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	public Integer getCount(String userId) {
		return workspaceCustomRepository.getCount(userId);
	}


	@Override
	public CodeServerWorkspaceVO getByUniqueliteral(String userId, String uniqueLiteral, String value) {
		if (value != null) {
			CodeServerWorkspaceNsql entity = workspaceCustomRepository.findbyUniqueLiteral(userId,uniqueLiteral, value);
			return workspaceAssembler.toVo(entity);
		} else
			return null;
	}


	@Override
	public GenericMessage deployWorspace(String id) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			Optional<CodeServerWorkspaceNsql> entityOptional = jpaRepo.findById(id);
			CodeServerWorkspaceNsql entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
			if(entity!=null) {
				GenericMessage jobResponse = client.performWorkBenchActions("deploy", entity.getData());
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					entity.getData().setStatus("DEPLOY_REQUESTED");
					jpaRepo.save(entity);
					status = "SUCCESS";
				}else {
					status = "FAILED";
					errors.addAll(jobResponse.getErrors());
				}
			}
		}catch(Exception e) {
				MessageDescription error = new MessageDescription();
				error.setMessage("Failed while deploying codeserver workspace project with exception " + e.getMessage());
				errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
		
	}
	
	@Override
	public GenericMessage undeployWorspace(String id) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			Optional<CodeServerWorkspaceNsql> entityOptional = jpaRepo.findById(id);
			CodeServerWorkspaceNsql entity = !entityOptional.isEmpty() ? entityOptional.get() : null;
			if(entity!=null) {
				GenericMessage jobResponse = client.performWorkBenchActions("undeploy", entity.getData());
				if(jobResponse!=null && "SUCCESS".equalsIgnoreCase(jobResponse.getSuccess())) {
					entity.getData().setStatus("UNDEPLOY_REQUESTED");
					jpaRepo.save(entity);
					status = "SUCCESS";
				}else {
					errors.addAll(jobResponse.getErrors());
				}
			}
		}catch(Exception e) {
				MessageDescription error = new MessageDescription();
				error.setMessage("Failed while deploying codeserver workspace project with exception " + e.getMessage());
				errors.add(error);
		}
		responseMessage.setErrors(errors);
		responseMessage.setWarnings(warnings);
		responseMessage.setSuccess(status);
		return responseMessage;
	}


	@Override
	public GenericMessage update(CodeServerWorkspaceVO existingVO) {
		GenericMessage responseMessage = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
		CodeServerWorkspaceNsql entity = workspaceAssembler.toEntity(existingVO);
		CodeServerWorkspaceNsql updatedEntity = jpaRepo.save(entity);
		status = "SUCCESS";
		log.info("Updated workspace status successfully");
		}catch(Exception e) {
			LOGGER.error("Error occured while updating workspace {} in database with exception {} ", existingVO.getName(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while updating workspace status in database with exception " + e.getMessage());
			errors.add(error);
		}
		responseMessage.setSuccess(status);
		responseMessage.setWarnings(warnings);
		responseMessage.setErrors(errors);
		return responseMessage;
	}

	
}
