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

package com.daimler.dna.airflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.daimler.dna.airflow.app.main.auth.UserStore;
import com.daimler.dna.airflow.client.AirflowGitClient;
import com.daimler.dna.airflow.dto.AirflowDagRetryRequestVO;
import com.daimler.dna.airflow.dto.AirflowDagRetryResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateResponseVO;
import com.daimler.dna.airflow.dto.AirflowDagUpdateVO;
import com.daimler.dna.airflow.dto.AirflowGITResponse;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.dto.AirflowRetryDagVo;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.models.Dag;
import com.daimler.dna.airflow.repository.DagRepository;
import com.daimler.dna.airflow.repository.DnaProjectRepository;

@Service
public class DagMgmtServiceImpl implements DagMgmtService {

	private static Logger LOGGER = LoggerFactory.getLogger(DagMgmtServiceImpl.class);

	@Autowired
	private UserStore userStore;

	@Autowired
	private AirflowGitClient airflowGitClient;

	@Autowired
	private DagRepository dagRepository;

	@Autowired
	private DnaProjectRepository dnaProjectRepository;

	@Autowired
	private DnaProjectService dnaProjectService;

	@Override
	public ResponseEntity<AirflowDagUpdateResponseVO> updateAirflowDag(AirflowDagUpdateVO dag) {
		LOGGER.trace("Entering updateAirflowDag.");
		AirflowDagUpdateResponseVO res = new AirflowDagUpdateResponseVO();
		HttpStatus status = null;
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		List<MessageDescription> validErrors = validateUpdate(dag, currentUser);
		if (ObjectUtils.isEmpty(validErrors)) {
			List<MessageDescription> errors = airflowGitClient.updateAirflowDag(dag, currentUser);
			if (ObjectUtils.isEmpty(errors)) {
				status = HttpStatus.CREATED;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			status = HttpStatus.BAD_REQUEST;
			res.setErrors(validErrors);
		}
		res.setData(dag);
		LOGGER.trace("Returning from updateAirflowDag.");
		return new ResponseEntity<AirflowDagUpdateResponseVO>(res, status);
	}

	private List<MessageDescription> validateUpdate(AirflowDagUpdateVO dag, AirflowProjectUserVO currentUser) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();

		if (!dnaProjectService.getPermissions(dag.getDagName(), currentUser.getUsername()).contains("can_edit")) {
			LOGGER.debug("Access Denied. You cannot perform this operation.");
			MessageDescription md = new MessageDescription();
			md.setMessage("Access Denied. You cannot perform this operation.");
			errors.add(md);
			return errors;
		}
		;

		if (Objects.nonNull(dag)) {
			if (!dag.getDagContent().replaceAll("\\s", "").contains("DAG(")) {
				LOGGER.debug("Invalid DAG {}", dag.getDagName());
				MessageDescription md = new MessageDescription();
				md.setMessage("Invalid DAG " + dag.getDagName());
				errors.add(md);
			}
			String dag_id = "dag_id='" + dag.getDagName() + "'";
			if (!dag.getDagContent().replaceAll("\\s", "").contains(dag_id)) {
				LOGGER.debug("dag_id should be same as DAG name for:{}", dag.getDagName());
				MessageDescription md = new MessageDescription();
				md.setMessage("dag_id should be same as DAG name for: " + dag.getDagName());
				errors.add(md);
			}

		}
		return errors;
	}

	/**
	 * <p>
	 * To delete DAG identified by DAG name
	 * </p>
	 * 
	 * @param dagName
	 * @param projectId
	 * @return ResponseEntity<GenericMessage>
	 */
	@Override
	@javax.transaction.Transactional
	public ResponseEntity<GenericMessage> deleteDag(String dagName, String projectId) {
		LOGGER.trace("Entering deleteDag.");
		HttpStatus httpStatus = null;
		GenericMessage successMsg = new GenericMessage();
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		List<MessageDescription> validateErrors = this.validateDelete(dagName, projectId, currentUser);
		if (ObjectUtils.isEmpty(validateErrors)) {
			// Deleting DAG from GIT
			AirflowGITResponse gitErrors = airflowGitClient.deleteAirflowDag(dagName, currentUser);
			if (ObjectUtils.isEmpty(gitErrors.getErrors())) {
				LOGGER.debug("Deleting Project mapping of DAG {} from db.", dagName);
				dnaProjectRepository.deletedag(dagName);
				successMsg.setSuccess("success");
				httpStatus = HttpStatus.OK;
//				try {
//					LOGGER.info("Deleting Project mapping of DAG from db.");
//					dnaProjectRepository.deletedag(dagName);
//					successMsg.setSuccess("success");
//					httpStatus = HttpStatus.OK;
//				} catch (Exception e) {
//					// Rollback commit
//					List<MessageDescription> rollbackErrors = airflowGitClient
//							.gitRollback((RevCommit) gitErrors.getGitCommitId());
//					
//					List<MessageDescription> dbDeleteErrors = new ArrayList<MessageDescription>();
//					dbDeleteErrors.add(new MessageDescription(e.getMessage()));
//					if (!ObjectUtils.isEmpty(rollbackErrors)) {
//						dbDeleteErrors.addAll(rollbackErrors);
//					}
//					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//					successMsg.setErrors(dbDeleteErrors);
//					return new ResponseEntity<GenericMessage>(successMsg, httpStatus);
//				}
			} else {
				successMsg.setErrors(gitErrors.getErrors());
				httpStatus = HttpStatus.BAD_REQUEST;
			}
		} else {
			successMsg.setErrors(validateErrors);
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		LOGGER.trace("Returning from deleteDag.");
		return new ResponseEntity<GenericMessage>(successMsg, httpStatus);
	}

	/**
	 * Validate before deleting DAG
	 * 
	 * @param dagName
	 * @param projectId
	 * @return @return {@code errors} if exists otherwise {@code null}
	 */
	private List<MessageDescription> validateDelete(String dagName, String projectId,
			AirflowProjectUserVO currentUser) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		MessageDescription message = null;
		// Check for permission userStore
		if (!dnaProjectService.getPermissions(dagName, currentUser.getUsername()).contains("can_edit")) {
			LOGGER.debug("Access Denied. You cannot perform this operation.");
			MessageDescription md = new MessageDescription();
			md.setMessage("Access Denied. You cannot perform this operation.");
			errors.add(md);
		}
		;
		// check for DAG
		Dag dag = dagRepository.findByDagId(dagName);
		if (dag != null) {
			if (dag.getIsPaused() != null && !dag.getIsPaused()) {
				LOGGER.debug("Cannot delete DAG as it is in running state.{}", dagName);
				message = new MessageDescription();
				message.setMessage("Cannot delete DAG as it is in running state.");
				errors.add(message);
			} else if (dag.getSchedulerLock() != null && dag.getSchedulerLock()) {
				LOGGER.debug("Cannot delete DAG as it is scheduled.{}", dagName);
				message = new MessageDescription();
				message.setMessage("Cannot delete DAG as it is scheduled.");
				errors.add(message);
			} else if (dag.getIsSubdag() != null && dag.getIsSubdag()) {
				LOGGER.debug("Cannot delete DAG as it is a subDAG delete parent DAG first.{}", dagName);
				// Dag parentDag = dagRepository.findByDagId(dag.getRootDagId());
				message = new MessageDescription();
				message.setMessage("Cannot delete DAG as it is a subDAG delete parent DAG first.");
				errors.add(message);
			}
		}

		return ObjectUtils.isEmpty(errors) ? null : errors;
	}

	@Override
	public AirflowRetryDagVo getPermission(String dagName, String projectId) {
		AirflowProjectVO airflowProjectVO = dnaProjectService.getByProjectId(projectId);
		AirflowRetryDagVo airflowRetryDagVo = new AirflowRetryDagVo();
		airflowProjectVO.getDags().forEach(dag -> {
			if (dag.getDagName().equalsIgnoreCase(dagName)) {
				BeanUtils.copyProperties(dag, airflowRetryDagVo);
			}
		});
		LOGGER.debug("Updating default permission for dag {} .",dagName);
		dnaProjectService.updatePermisions(airflowRetryDagVo, projectId, true);
		LOGGER.debug("Successfully updated.");
		return airflowRetryDagVo;
	}

	@Override
	public ResponseEntity<AirflowDagRetryResponseVO> updatePermission(String dagName, String projectId,
			AirflowRetryDagVo airflowRetryDagVo) {
		AirflowDagRetryResponseVO res = new AirflowDagRetryResponseVO();
		try {
			dnaProjectService.updatePermisions(airflowRetryDagVo, projectId, false);
			res.setData(airflowRetryDagVo);
			res.setStatus("SUCCESS");
		} catch (Exception e) {
			LOGGER.error("Unable to updage permission please try again..{}", e.getMessage());
			MessageDescription errorsItem = new MessageDescription();
			errorsItem.setMessage("Unable to updage permission please try again..");
			res.addErrorsItem(errorsItem);
			res.setStatus("FAILURE");
			new ResponseEntity<AirflowDagRetryResponseVO>(res, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<AirflowDagRetryResponseVO>(res, HttpStatus.OK);
	}

}
