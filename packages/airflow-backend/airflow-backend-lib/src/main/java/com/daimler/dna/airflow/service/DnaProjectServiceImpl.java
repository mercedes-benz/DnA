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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.eclipse.jgit.revwalk.RevCommit;
import org.postgresql.shaded.com.ongres.scram.common.message.ServerFinalMessage.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.dna.airflow.app.main.auth.UserStore;
import com.daimler.dna.airflow.assembler.DnaProjectAssesmbler;
import com.daimler.dna.airflow.assembler.RoleAssembler;
import com.daimler.dna.airflow.assembler.UserAssembler;
import com.daimler.dna.airflow.controller.AirflowPermissionsController;

import com.daimler.dna.airflow.client.AirflowGitClient;
import com.daimler.dna.airflow.dto.AirflowDagProjectResponseVo;
import com.daimler.dna.airflow.dto.AirflowDagVo;
import com.daimler.dna.airflow.dto.AirflowGITResponse;
import com.daimler.dna.airflow.dto.AirflowProjectRequestVO;
import com.daimler.dna.airflow.dto.AirflowProjectResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.dto.AirflowProjectsByUserVO;
import com.daimler.dna.airflow.dto.AirflowResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRetryDagVo;
import com.daimler.dna.airflow.dto.CreatedByVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.models.Dag;
import com.daimler.dna.airflow.models.DnaProject;
import com.daimler.dna.airflow.models.DnaProjectAndUserMapping;
import com.daimler.dna.airflow.models.DnaProjectUserAndDagMapping;
import com.daimler.dna.airflow.models.ImportError;
import com.daimler.dna.airflow.models.PermissionAndRoleMapping;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.models.User;
import com.daimler.dna.airflow.models.UserRoleMapping;
import com.daimler.dna.airflow.models.ViewMenu;
import com.daimler.dna.airflow.repository.DagRepository;
import com.daimler.dna.airflow.repository.DnaProjectAndUserMappingRepository;
import com.daimler.dna.airflow.repository.DnaProjectRepository;
import com.daimler.dna.airflow.repository.IDnaProjectRepository;
import com.daimler.dna.airflow.repository.ImportErrorRepository;
import com.daimler.dna.airflow.repository.PermissionAndRoleMappingRepository;
import com.daimler.dna.airflow.repository.RoleRepository;
import com.daimler.dna.airflow.repository.UserAndDagMappingRepository;
import com.daimler.dna.airflow.repository.UserRepository;
import com.daimler.dna.airflow.repository.UserRoleMappingRepository;
import com.daimler.dna.airflow.repository.ViewMenuRepository;

@Service
public class DnaProjectServiceImpl implements DnaProjectService {

	private static Logger LOGGER = LoggerFactory.getLogger(DnaProjectServiceImpl.class);

	@Value("${dag.menuCreate.waitTime}")
	private Integer waitTime;

	@Value("${dag.menuCreate.reTry}")
	private int reTry;

	@Autowired
	private DnaProjectRepository dnaProjectRepository;

	@Autowired
	private IDnaProjectRepository iDnaProjectRepository;

	@Autowired
	private DnaProjectAssesmbler assembler;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleAssembler roleAssembler;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserAssembler userAssembler;

	@Autowired
	private ViewMenuRepository viewMenuRepository;

	@Autowired
	private UserRoleMappingRepository userRoleMappingRepository;

	@Autowired
	private PermissionAndRoleMappingRepository permissionAndRoleMappingRepository;

	@Autowired
	private DagRepository dagRepository;

	@Autowired
	private UserAndDagMappingRepository userAndDagMappingRepository;

	@Autowired
	private DnaProjectAndUserMappingRepository dnaProjectAndUserMappingRepository;

	@Autowired
	private UserStore userStore;

	@Autowired
	private AirflowGitClient airflowGitClient;

	@Autowired
	private ImportErrorRepository importErrorRepository;

	@Override
	public List<AirflowProjectsByUserVO> getAllProjects(int offset, int limit) {
		LOGGER.trace("Processing getAllProjects...");
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		Map<String, AirflowProjectsByUserVO> map = new HashMap<String, AirflowProjectsByUserVO>();
		List<AirflowProjectsByUserVO> list = new ArrayList<AirflowProjectsByUserVO>();
		List<AirflowDagProjectResponseVo> dags = null;
		LOGGER.debug("fetching project details for {} from database.", currentUser.getUsername());
		List<Object[]> result = dnaProjectRepository.findAllProjectsByUserId(currentUser.getUsername());
		LOGGER.debug("Database fetch successfull {}", currentUser.getUsername());
		for (Object[] obj : result) {
			map.put((String) obj[0], assembler.toVO(obj, currentUser.getUsername(), map));
		}
		for (Map.Entry<String, AirflowProjectsByUserVO> entry : map.entrySet()) {

			Map<String, List<String>> dagMap = new HashMap<>();
			List<AirflowDagProjectResponseVo> listOfDag = new ArrayList<AirflowDagProjectResponseVo>();
			for (AirflowDagProjectResponseVo dag : entry.getValue().getDags()) {
				List<String> listPermission = null;
				if (dagMap.containsKey(dag.getDagName())) {
					listPermission = dagMap.get(dag.getDagName());
					listPermission.add(dag.getPermissions().get(0));
				} else {
					dagMap.put(dag.getDagName(), dag.getPermissions());
				}

			}
			for (Map.Entry<String, List<String>> dagEntry : dagMap.entrySet()) {
				AirflowDagProjectResponseVo dagVo = new AirflowDagProjectResponseVo();
				dagVo.setDagName(dagEntry.getKey());
				List<Object[]> permissions = dnaProjectRepository.dagPermissionList(dagEntry.getKey(),
						currentUser.getUsername());
				for (Object[] objArray : permissions) {
					dagVo.addPermissionsItem(objArray[0].toString());
				}
				listOfDag.add(dagVo);
			}
			entry.getValue().setDags(listOfDag);

			list.add(entry.getValue());
		}
		LOGGER.trace("Successfully processed getAllProjects.");
		return list;
	}

	@Override
	public AirflowProjectVO getByProjectId(String projectId) {
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		List<DnaProject> projects = dnaProjectRepository.findbyUniqueLiteral("projectId", projectId);
		projects = projects.stream().filter(p -> p.getCreatedBy().equalsIgnoreCase(currentUser.getUsername()))
				.collect(Collectors.toList());
		return (Objects.nonNull(projects) && !projects.isEmpty()) ? assembler.toVO(projects.get(0)) : null;
	}

	@Override
	@Transactional
	public ResponseEntity<AirflowProjectResponseWrapperVO> createAirflowProject(AirflowProjectVO airflowProjectVO) {
		AirflowProjectResponseWrapperVO res = new AirflowProjectResponseWrapperVO();
		List<MessageDescription> warnings = new ArrayList<MessageDescription>();
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		airflowProjectVO.setCreatedBy(currentUser.getUsername());
		List<MessageDescription> validErrors = validateProject(airflowProjectVO);
		if (ObjectUtils.isEmpty(validErrors)) {
			List<MessageDescription> errors = airflowGitClient.createAirflowDags(airflowProjectVO, currentUser);
			if (ObjectUtils.isEmpty(errors)) {
				DnaProject savedDnaProject = dnaProjectRepository.save(assembler.toEntity(airflowProjectVO));
				List<String> permissions = new ArrayList<>();
				permissions.add("can_dag_edit");
				permissions.add("can_dag_read");
				currentUser.setPermissions(permissions);
				boolean isCurrentUserExist = userExist(currentUser.getEmail());
				boolean isCurrentRoleExist = roleExist(currentUser.getUsername());
				Role savedCurrentUserRole = isCurrentRoleExist ? findRole(currentUser.getUsername())
						: addRole(currentUser.getUsername());
				User savedCurrentUser = isCurrentUserExist ? findUser(currentUser.getEmail()) : addUser(currentUser);
				if (!isCurrentUserExist && !isCurrentRoleExist && Objects.nonNull(savedCurrentUser)
						&& Objects.nonNull(savedCurrentUserRole)) {
					addUserRoleMapping(savedCurrentUser, savedCurrentUserRole);
				}
				// for each dag
				for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
					boolean isPermissionCreated = false;
					boolean isDagExist = false;
					boolean isErrorExist = false;
					int rerun = reTry;
					while (rerun > 0) {
						isErrorExist = isErrorExistInDag(dagVO.getDagName());
						isDagExist = checkDagMenu(dagVO.getDagName());
						if (!isErrorExist && isDagExist) {
							isPermissionCreated = checkDagPermissionAndViewMenu(dagVO);
							if (isPermissionCreated) {
								break;
							} else {
								try {
									LOGGER.debug("waiting for {} sec to create permission", waitTime);
									Thread.sleep(waitTime * 1000);
									rerun--;
								} catch (InterruptedException e) {
									LOGGER.error("Unable to stop {}", e.getMessage());
								}
							}
						} else if (!isErrorExist && !isDagExist) {
							try {
								LOGGER.debug("waiting for {} sec to create permission", waitTime);
								Thread.sleep(waitTime * 1000);
								rerun--;
							} catch (InterruptedException e) {
								LOGGER.error("Unable to stop {}", e.getMessage());
							}
						} else {
							airflowGitClient.deleteAirflowDag(dagVO.getDagName(), currentUser);
							dnaProjectRepository.delete(savedDnaProject);
							res.setData(airflowProjectVO);
							errors = checkForDagSyntaxError(dagVO.getDagName());
							res.setErrors(errors);
							res.setStatus("FAILURE");
							return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
						}
					}
					if (isDagExist) {
						LOGGER.debug("mapping dag and user to project..");
						addPermissionAndMappedToProject(currentUser, dagVO, savedCurrentUserRole, savedCurrentUser,
								savedDnaProject);
					} else {
						LOGGER.debug("Unable to create project. Please create after sometime.");
						airflowGitClient.deleteAirflowDag(dagVO.getDagName(), currentUser);
						dnaProjectRepository.delete(savedDnaProject);
						res.setData(airflowProjectVO);
						List<MessageDescription> errors1 = new ArrayList<MessageDescription>();
						MessageDescription md = new MessageDescription();
						md.setMessage("Unable to create project. Please create after sometime.");
						errors1.add(md);
						res.setErrors(errors1);
						res.setStatus("FAILURE");
						return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
					}
					if (isPermissionCreated) {
						LOGGER.debug("mapping permission to role..");
						addRoleAndPermissionMapping(currentUser, savedCurrentUserRole, dagVO.getDagName());
					} else {
						LOGGER.warn(
								"Permission is not created for the DAG {} , however project is onboarded. Please contact administrator for permission.",
								dagVO.getDagName());
						MessageDescription md = new MessageDescription();
						md.setMessage("Permission is not created for the DAG" + dagVO.getDagName()
								+ ", however project is onboarded. Please contact administrator for permission.");
						warnings.add(md);
					}
					if (!ObjectUtils.isEmpty(dagVO.getCollaborators())) {
						for (AirflowProjectUserVO userVO : dagVO.getCollaborators()) {
							boolean isRoleExist = roleExist(userVO.getUsername());
							boolean isUserExist = userExist(userVO.getEmail());
							Role savedRole = isRoleExist ? findRole(userVO.getUsername())
									: addRole(userVO.getUsername());
							User savedUser = isUserExist ? findUser(userVO.getEmail()) : addUser(userVO);
							if (!isUserExist && !isRoleExist && Objects.nonNull(savedUser)
									&& Objects.nonNull(savedRole)) {
								addUserRoleMapping(savedUser, savedRole);
							}
							LOGGER.debug("User onboarded successfully..{}", savedUser.getUsername());
							if (isDagExist) {
								LOGGER.debug("mapping dag and user to project..");
								addPermissionAndMappedToProject(userVO, dagVO, savedRole, savedUser, savedDnaProject);
							}
							if (isPermissionCreated) {
								LOGGER.debug("mapping permission to role..");
								addRoleAndPermissionMapping(userVO, savedRole, dagVO.getDagName());
							}
						}
					}
				}
			} else {
				res.setData(airflowProjectVO);
				res.setErrors(errors);
				res.setStatus("FAILURE");
				return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
			}

		} else {
			res.setData(airflowProjectVO);
			res.setErrors(validErrors);
			res.setStatus("FAILURE");
			return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
		}
		res.setWarnings(warnings);
		res.setData(airflowProjectVO);
		res.setStatus("SUCCESS");
		LOGGER.info("Project {} successfully created.", airflowProjectVO.getProjectName());
		return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.CREATED);
	}

	@Override
	@Transactional
	public ResponseEntity<AirflowProjectResponseWrapperVO> updateAirflowProject(AirflowProjectVO airflowProjectVO,
			String projectId) {
		AirflowProjectResponseWrapperVO res = new AirflowProjectResponseWrapperVO();
		List<MessageDescription> warnings = new ArrayList<MessageDescription>();
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		airflowProjectVO.setCreatedBy(currentUser.getUsername());
		List<MessageDescription> validErrors = validateProjectForUpdate(airflowProjectVO, projectId);
		if (ObjectUtils.isEmpty(validErrors)) {
			AirflowGITResponse airflowGITResponse = airflowGitClient.updateAirflowDags(airflowProjectVO, currentUser);
			if (ObjectUtils.isEmpty(airflowGITResponse.getErrors())) {
				DnaProject existingDnaProject = findProject(projectId);
				DnaProject updatedProject = assembler.toEntity(airflowProjectVO);
				deleteExistingRoleAndPermission(existingDnaProject, airflowProjectVO);
				deleteAllProjectAndUserMapping(existingDnaProject);
				List<String> permissions = new ArrayList<>();
				permissions.add("can_dag_edit");
				permissions.add("can_dag_read");
				currentUser.setPermissions(permissions);
				boolean isCurrentUserExist = userExist(currentUser.getEmail());
				boolean isCurrentRoleExist = roleExist(currentUser.getUsername());
				Role savedCurrentUserRole = isCurrentRoleExist ? findRole(currentUser.getUsername())
						: addRole(currentUser.getUsername());
				User savedCurrentUser = isCurrentUserExist ? findUser(currentUser.getEmail()) : addUser(currentUser);
				if (!isCurrentUserExist && !isCurrentRoleExist && Objects.nonNull(savedCurrentUser)
						&& Objects.nonNull(savedCurrentUserRole)) {
					addUserRoleMapping(savedCurrentUser, savedCurrentUserRole);
				}
				for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
					boolean isErrorExist = false;
					boolean isPermissionCreated = false;
					boolean isDagExist = false;
					int rerun = reTry;
					while (rerun > 0) {
						isErrorExist = isErrorExistInDag(dagVO.getDagName());
						isDagExist = checkDagMenu(dagVO.getDagName());

						if (!isErrorExist && isDagExist) {
							isPermissionCreated = checkDagPermissionAndViewMenu(dagVO);
							if (isPermissionCreated) {
								break;
							} else {
								try {
									LOGGER.debug("waiting for {} sec to create permission", waitTime);
									Thread.sleep(waitTime * 1000);
									rerun--;
								} catch (InterruptedException e) {
									LOGGER.error("Unable to stop {}", e.getMessage());
								}
							}
						} else if (!isErrorExist && !isDagExist) {
							try {
								LOGGER.debug("waiting for {} sec to create permission", waitTime);
								Thread.sleep(waitTime * 1000);
								rerun--;
							} catch (InterruptedException e) {
								LOGGER.error("Unable to stop {}", e.getMessage());
							}
						} else {
							airflowGitClient.gitRollback((RevCommit) airflowGITResponse.getGitCommitId());
							res.setData(airflowProjectVO);
							errors = checkForDagSyntaxError(airflowProjectVO.getDags());
							res.setErrors(errors);
							res.setStatus("FAILURE");
							return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
						}

					}

					if (isDagExist) {
						updatePermissionAndMappedToProject(currentUser, dagVO, savedCurrentUserRole, savedCurrentUser,
								existingDnaProject);
					} else {
						LOGGER.debug("Unable to update project. Please create after sometime.");
						airflowGitClient.gitRollback((RevCommit) airflowGITResponse.getGitCommitId());
						res.setData(airflowProjectVO);
						List<MessageDescription> errors1 = new ArrayList<MessageDescription>();
						MessageDescription md = new MessageDescription();
						md.setMessage("Unable to update project. Please create after sometime.");
						errors1.add(md);
						res.setErrors(errors1);
						res.setStatus("FAILURE");
						return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
					}
					if (isPermissionCreated) {
						deleteRoleAndPermissionMapping(savedCurrentUserRole, dagVO.getDagName());
						LOGGER.debug("mapping permission to role..");
						updateRoleAndPermissionMapping(currentUser, savedCurrentUserRole, dagVO.getDagName(),
								existingDnaProject);
					} else {
						LOGGER.warn(
								"Permission is not created for the DAG {} , however project is onboarded. Please contact administrator for permission.",
								dagVO.getDagName());
						MessageDescription md = new MessageDescription();
						md.setMessage("Permission is not created for the DAG" + dagVO.getDagName()
								+ ", however project is updated. Please contact administrator for permission.");
						warnings.add(md);
					}
					if (!ObjectUtils.isEmpty(dagVO.getCollaborators())) {
						for (AirflowProjectUserVO userVO : dagVO.getCollaborators()) {
							boolean isRoleExist = roleExist(userVO.getUsername());
							boolean isUserExist = userExist(userVO.getEmail());
							Role savedRole = isRoleExist ? findRole(userVO.getUsername())
									: addRole(userVO.getUsername());
							User savedUser = isUserExist ? findUser(userVO.getEmail()) : addUser(userVO);
							if (!isUserExist && !isRoleExist && Objects.nonNull(savedUser)
									&& Objects.nonNull(savedRole)) {
								addUserRoleMapping(savedUser, savedRole);
							}
							LOGGER.debug("User onboarded successfully..{}", savedUser.getUsername());
							LOGGER.debug("mapping dag and user to project.."); // find permissionView menu
							if (isDagExist && !currentUser.getUsername().equalsIgnoreCase(userVO.getUsername())) {
								LOGGER.debug("mapping dag and user to project..");
								updatePermissionAndMappedToProject(userVO, dagVO, savedRole, savedUser,
										existingDnaProject);
							}
							if (isPermissionCreated
									&& !currentUser.getUsername().equalsIgnoreCase(userVO.getUsername())) {
								deleteRoleAndPermissionMapping(savedRole, dagVO.getDagName());
								LOGGER.debug("mapping permission to role..");
								updateRoleAndPermissionMapping(userVO, savedRole, dagVO.getDagName(),
										existingDnaProject);
							}
						}
					}

					LOGGER.debug("updating dna project");
					updateProject(updatedProject);
//					/dnaProjectRepository.save(updatedProject);
				}
			} else {
				res.setData(airflowProjectVO);
				res.setErrors(airflowGITResponse.getErrors());
				res.setStatus("FAILURE");
				return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
			}

		} else {
			res.setData(airflowProjectVO);
			res.setErrors(validErrors);
			res.setStatus("FAILURE");
			return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.BAD_REQUEST);
		}
		res.setWarnings(warnings);
		res.setData(airflowProjectVO);
		res.setStatus("SUCCESS");
		LOGGER.info("Project {} successfully updated.", airflowProjectVO.getProjectName());
		return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.OK);
	}

	@Override
	@Transactional
	public void updatePermisions(AirflowRetryDagVo airflowRetryDagVo, String projectId, boolean defaultPermission) {
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		DnaProject existingDnaProject = findProject(projectId);
		List<String> permissions = new ArrayList<>();
		permissions.add("can_dag_edit");
		permissions.add("can_dag_read");
		currentUser.setPermissions(permissions);
		boolean isCurrentRoleExist = roleExist(currentUser.getUsername());
		Role savedCurrentUserRole = isCurrentRoleExist ? findRole(currentUser.getUsername())
				: addRole(currentUser.getUsername());
		deleteRoleAndPermissionMapping(savedCurrentUserRole, airflowRetryDagVo.getDagName());
		LOGGER.debug("mapping permission to role..");
		updateRoleAndPermissionMapping(currentUser, savedCurrentUserRole, airflowRetryDagVo.getDagName(),
				existingDnaProject);
		if (!ObjectUtils.isEmpty(airflowRetryDagVo.getCollaborators())) {
			for (AirflowProjectUserVO userVO : airflowRetryDagVo.getCollaborators()) {
				if (defaultPermission) {
					userVO.setPermissions(permissions);
				}
				boolean isRoleExist = roleExist(userVO.getUsername());
				Role savedRole = isRoleExist ? findRole(userVO.getUsername()) : addRole(userVO.getUsername());
				deleteRoleAndPermissionMapping(savedRole, airflowRetryDagVo.getDagName());
				LOGGER.debug("mapping permission to role..");
				updateRoleAndPermissionMapping(userVO, savedRole, airflowRetryDagVo.getDagName(), existingDnaProject);
			}
		}
	}

	private void deleteExistingRoleAndPermission(DnaProject existingDnaProject, AirflowProjectVO airflowProjectVO) {
		LOGGER.debug("deleting permission for all the roles ..");
		for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
			existingDnaProject.getDnaProjectUserMappings().forEach(
					dagMapping -> dagMapping.getDnaProjectUserAndDagMapping().getUser().getRoleMaping().forEach(
							roleMapping -> deleteRoleAndPermissionMapping(roleMapping.getRole(), dagVO.getDagName())));
			LOGGER.debug("Successfully deleted all the role and permission mapping for the dag {}", dagVO.getDagName());
		}
	}

	private void updateProject(DnaProject updatedProject) {
		LOGGER.debug("updating existing project");
		dnaProjectRepository.updateProject(updatedProject);
	}

	private List<MessageDescription> validateProjectForUpdate(AirflowProjectVO airflowProjectVO, String projectId) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		if (!projectExist(projectId)) {
			LOGGER.debug("Project id does not exist.");
			MessageDescription messageDescription = new MessageDescription();
			messageDescription.setMessage("Project id does not exist.");
			errors.add(messageDescription);
		}
		return errors;
	}

	private List<MessageDescription> validateProject(AirflowProjectVO airflowProjectVO) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		if (projectExist(airflowProjectVO.getProjectId())) {
			LOGGER.debug("Project id already exist.");
			MessageDescription messageDescription = new MessageDescription();
			messageDescription.setMessage("Project id already exist.");
			errors.add(messageDescription);
		}

		if (!ObjectUtils.isEmpty(iDnaProjectRepository.getDnaProjectByUserNameAndProjectName(
				airflowProjectVO.getCreatedBy(), airflowProjectVO.getProjectName()))) {
			LOGGER.debug("Project with the name {} already exists. Please use different name.",
					airflowProjectVO.getProjectName());
			MessageDescription messageDescription = new MessageDescription();
			messageDescription.setMessage("Project with the name " + airflowProjectVO.getProjectName()
					+ " already exists. Please use different name.");
			errors.add(messageDescription);
		}

		if (ObjectUtils.isEmpty(airflowProjectVO.getDags())) {
			LOGGER.debug("Atleast one dag should be present.");
			MessageDescription messageDescription = new MessageDescription();
			messageDescription.setMessage("Atleast one dag should be present.");
			errors.add(messageDescription);
		}

		for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
			if (dagNameExist(dagVO.getDagName())) {
				LOGGER.debug("Dag name {} already exist!", dagVO.getDagName());
				MessageDescription messageDescription = new MessageDescription();
				messageDescription.setMessage("Dag name " + dagVO.getDagName() + " already exist!");
				errors.add(messageDescription);
			}
		}

		if (!ObjectUtils.isEmpty(airflowProjectVO.getDags())) {
			String dag_id = "";
			for (AirflowDagVo dag : airflowProjectVO.getDags()) { // dag_id='DemoDag'
				if (!dag.getDagContent().replaceAll("\\s", "").contains("DAG(")) {
					LOGGER.debug("Invalid DAG {}", dag.getDagName());
					MessageDescription md = new MessageDescription();
					md.setMessage("Invalid DAG " + dag.getDagName());
					errors.add(md);
				}

				dag_id = "dag_id='" + dag.getDagName() + "'";
				if (!dag.getDagContent().replaceAll("\\s", "").contains(dag_id)) {
					LOGGER.debug("dag_id should be same as DAG name for {}", dag.getDagName());
					MessageDescription md = new MessageDescription();
					md.setMessage("dag_id should be same as DAG name for: " + dag.getDagName());
					errors.add(md);
				}
			}
		}

		if (!ObjectUtils.isEmpty(airflowProjectVO.getDags()) && airflowProjectVO.getDags().size() > 1) {
			LOGGER.debug("Only one dag is allowed while creating project.");
			MessageDescription messageDescription = new MessageDescription();
			messageDescription.setMessage("Only one dag is allowed while creating project.");
			errors.add(messageDescription);
		}

		return errors;
	}

	private boolean dagNameExist(String dagName) {
		Dag dag = dagRepository.findByDagId(dagName);
		return Objects.nonNull(dag);
	}

	private boolean checkDagMenu(String dagName) {
		Dag dag = dagRepository.findByDagId(dagName);
		return !ObjectUtils.isEmpty(dag);
	}

	private boolean projectExist(String projectId) {
		List<DnaProject> projects = dnaProjectRepository.findbyUniqueLiteral("projectId", projectId);
		return Objects.nonNull(projects) && !projects.isEmpty();
	}

	private DnaProject findProject(String projectId) {
		List<DnaProject> projects = dnaProjectRepository.findbyUniqueLiteral("projectId", projectId);
		return Objects.nonNull(projects) && !projects.isEmpty() ? projects.get(0) : null;
	}

	private boolean userExist(String email) {
		List<User> existingUser = userRepository.findbyUniqueLiteral("email", email);
		return Objects.nonNull(existingUser) && !existingUser.isEmpty();
	}

	private User addUser(AirflowProjectUserVO userVO) {
		User savedUser = null;
		LOGGER.debug("creating user ..{}", userVO.getUsername());
		savedUser = userRepository.save(userAssembler.toEntity(userVO));
		LOGGER.debug("successfully created user {}", savedUser.getUsername());
		return savedUser;
	}

	private User findUser(String email) {
		List<User> existingUser = userRepository.findbyUniqueLiteral("email", email);
		return Objects.nonNull(existingUser) && !existingUser.isEmpty() ? existingUser.get(0) : null;
	}

	private boolean roleExist(String name) {
		Role existingRole = roleRepository.findByName(name);
		return Objects.nonNull(existingRole);
	}

	private Role addRole(String name) {
		LOGGER.debug("creating role..{}", name);
		Role savedRole = roleRepository.save(roleAssembler.toEntity(name));
		LOGGER.debug("role created {}", name);
		return savedRole;
	}

	private Role findRole(String name) {
		Role existingRole = roleRepository.findByName(name);
		return existingRole;
	}

	private UserRoleMapping addUserRoleMapping(User savedUser, Role savedRole) {
		LOGGER.debug("creating user role mapping..");
		UserRoleMapping userRoleMapping = new UserRoleMapping();
		userRoleMapping.setRole(savedRole);
		userRoleMapping.setUser(savedUser);
		UserRoleMapping savedUserRoleMapping = userRoleMappingRepository.save(userRoleMapping);
		LOGGER.debug("User and Role mapping successfully created..");
		return savedUserRoleMapping;
	}

	private PermissionAndRoleMapping addRoleAndPermissionMapping(AirflowProjectUserVO userVO, Role savedRole,
			String dagName) {
		LOGGER.debug("adding role and permission ..");
		ViewMenu viewMenu = viewMenuRepository.findByName(dagName);
		PermissionAndRoleMapping savePermissionAndRoleMapping = null;
		if (Objects.nonNull(viewMenu)) {
			for (String permission : userVO.getPermissions()) {
				LOGGER.debug("role {} and permission {}", savedRole.getName(), permission);
				PermissionAndRoleMapping entity = new PermissionAndRoleMapping();
				entity.setRole(savedRole);
				entity.setPermissionView(viewMenu.getPermissionAndViewMenuMappings().stream()
						.filter(x -> x.getPermission().getName().equalsIgnoreCase(permission))
						.collect(Collectors.toList()).get(0));
				savePermissionAndRoleMapping = permissionAndRoleMappingRepository.save(entity);
				LOGGER.debug("successfully mapped permission and role..");
			}
		}
		return savePermissionAndRoleMapping;
	}

	private PermissionAndRoleMapping updateRoleAndPermissionMapping(AirflowProjectUserVO userVO, Role savedRole,
			String dagName, DnaProject exiatingDnaProject) {
		LOGGER.debug("adding role and permission ..");
		ViewMenu viewMenu = viewMenuRepository.findByName(dagName);
		PermissionAndRoleMapping savePermissionAndRoleMapping = null;
		if (Objects.nonNull(viewMenu)) {
			for (String permission : userVO.getPermissions()) {
				LOGGER.debug("role {} and permission {}", savedRole.getName(), permission);
				PermissionAndRoleMapping entity = new PermissionAndRoleMapping();
				entity.setRole(savedRole);
				entity.setPermissionView(viewMenu.getPermissionAndViewMenuMappings().stream()
						.filter(x -> x.getPermission().getName().equalsIgnoreCase(permission))
						.collect(Collectors.toList()).get(0));
				savePermissionAndRoleMapping = permissionAndRoleMappingRepository.save(entity);
				LOGGER.debug("successfully mapped permission and role..");
			}
		}
		return savePermissionAndRoleMapping;
	}

	private void deleteRoleAndPermissionMapping(Role savedRole, String dagName) {
		LOGGER.debug("deleting role and permission mapping for {}", dagName);
		if (savedRole.getPermissionAndRoleMapping() != null) {
			for (PermissionAndRoleMapping permission : savedRole.getPermissionAndRoleMapping()) {
				LOGGER.debug("role {} ", savedRole.getName());
				if (permission.getPermissionView().getViewMenu().getName().equalsIgnoreCase(dagName)) {
					Integer result = dnaProjectRepository.deleteRoleAndPermission(permission.getId());
					LOGGER.debug("successfully deleted permission for DAG {}  from  role {} no of row ", dagName,
							savedRole.getName(), result);

				}
			}
		}
	}

	private DnaProjectUserAndDagMapping addUserAndDagMapping(User savedUser, String dagName) {
		Dag dag = dagRepository.findByDagId(dagName);
		DnaProjectUserAndDagMapping dnaProjectUserAndDagMapping = new DnaProjectUserAndDagMapping();
		dnaProjectUserAndDagMapping.setDag(dag);
		dnaProjectUserAndDagMapping.setUser(savedUser);
		DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping = userAndDagMappingRepository
				.save(dnaProjectUserAndDagMapping);
		LOGGER.debug("Successfully created User and Dag mapping..");
		return savedDnaProjectUserAndDagMapping;
	}

	private DnaProjectUserAndDagMapping updateUserAndDagMapping(User savedUser, String dagName) {
		Dag dag = dagRepository.findByDagId(dagName);
		if (Objects.isNull(dag)) {
			LOGGER.debug("dag still not created trying to created after 2 sec..");
		}
		DnaProjectUserAndDagMapping dnaProjectUserAndDagMapping = new DnaProjectUserAndDagMapping();
		dnaProjectUserAndDagMapping.setDag(dag);
		dnaProjectUserAndDagMapping.setUser(savedUser);
		DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping = userAndDagMappingRepository
				.save(dnaProjectUserAndDagMapping);
		LOGGER.debug("Successfully created User and Dag mapping..");
		return savedDnaProjectUserAndDagMapping;
	}

	private DnaProjectAndUserMapping addProjectAndUserDagMapping(DnaProject savedDnaProject,
			DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping) {
		LOGGER.debug("adding project and User and dag mapping.");
		DnaProjectAndUserMapping dnaProjectAndUserMapping = new DnaProjectAndUserMapping();
		dnaProjectAndUserMapping.setDnaProject(savedDnaProject);
		dnaProjectAndUserMapping.setDnaProjectUserAndDagMapping(savedDnaProjectUserAndDagMapping);
		DnaProjectAndUserMapping savedDnaProjectAndUserMapping = dnaProjectAndUserMappingRepository
				.save(dnaProjectAndUserMapping);
		LOGGER.debug("Successfully created savedDnaProjectAndUserMapping..");
		return savedDnaProjectAndUserMapping;
	}

	private DnaProjectAndUserMapping updateProjectAndUserDagMapping(DnaProject savedDnaProject,
			DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping) {
		LOGGER.debug("adding project and User and dag mapping.");
		DnaProjectAndUserMapping dnaProjectAndUserMapping = new DnaProjectAndUserMapping();
		dnaProjectAndUserMapping.setDnaProject(savedDnaProject);
		dnaProjectAndUserMapping.setDnaProjectUserAndDagMapping(savedDnaProjectUserAndDagMapping);
		DnaProjectAndUserMapping savedDnaProjectAndUserMapping = dnaProjectAndUserMappingRepository
				.save(dnaProjectAndUserMapping);
		LOGGER.debug("Successfully created savedDnaProjectAndUserMapping..");
		return savedDnaProjectAndUserMapping;
	}

	private void addPermissionAndMappedToProject(AirflowProjectUserVO userVO, AirflowDagVo dagVO, Role savedRole,
			User savedUser, DnaProject savedDnaProject) {
		// addRoleAndPermissionMapping(userVO, savedRole, dagVO.getDagName());
		DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping = addUserAndDagMapping(savedUser,
				dagVO.getDagName());
		addProjectAndUserDagMapping(savedDnaProject, savedDnaProjectUserAndDagMapping);
	}

	private void updatePermissionAndMappedToProject(AirflowProjectUserVO userVO, AirflowDagVo dagVO, Role savedRole,
			User savedUser, DnaProject existingDnaProject) {
		DnaProjectUserAndDagMapping savedDnaProjectUserAndDagMapping = updateUserAndDagMapping(savedUser,
				dagVO.getDagName());
		updateProjectAndUserDagMapping(existingDnaProject, savedDnaProjectUserAndDagMapping);
	}

	private void deleteAllProjectAndUserMapping(DnaProject existingDnaProject) {
		LOGGER.debug("deleting mapping from project");
		List<Integer> ids = new ArrayList<>();
		for (DnaProjectAndUserMapping mapping : existingDnaProject.getDnaProjectUserMappings()) {
			ids.add(mapping.getDnaProjectUserAndDagMapping().getId());
		}
		Integer result = dnaProjectRepository.deleteUserAndProjectMapping(existingDnaProject.getId());
		ids.forEach(x -> dnaProjectRepository.deleteUserAndDagMapping(x));
		LOGGER.debug("successfully deleted mapping from project {}", result);
	}

	private boolean checkDagPermissionAndViewMenu(AirflowDagVo dagVO) {
		LOGGER.debug("Find menu mapping for this {}", dagVO.getDagName());
		List<Object[]> list = dnaProjectRepository.findDagPermissionAndViewMenu(dagVO.getDagName());
		LOGGER.debug("is permission created. {}", ObjectUtils.isEmpty(list));
		return !ObjectUtils.isEmpty(list);
	}

	@Override
	public String getProjectId() {
		StringBuilder sb = new StringBuilder("P");
		BigDecimal seqNo = iDnaProjectRepository.getNext();
		int n = seqNo.precision();
		int x = 6 - n;
		while (x > 0) {
			sb.append("0");
			x--;
		}
		sb.append(seqNo);
		return sb.toString();
	}

	private boolean isErrorExistInDag(String dagName) {
		List<ImportError> listOfError = importErrorRepository.findByDagName(dagName);
		return !ObjectUtils.isEmpty(listOfError);
	}

	private List<MessageDescription> checkForDagSyntaxError(String dagName) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		List<ImportError> listOfError = importErrorRepository.findByDagName(dagName);
		listOfError.forEach(error -> {
			MessageDescription md = new MessageDescription();
			md.setMessage("Broken DAG: [" + error.getFilename() + "]" + " " + error.getStacktrace());
			errors.add(md);
		});
		return errors;
	}

	private List<MessageDescription> checkForDagSyntaxError(List<AirflowDagVo> listOfDagVo) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		listOfDagVo.forEach(dag -> {
			List<ImportError> listOfError = importErrorRepository.findByDagName(dag.getDagName());
			listOfError.forEach(error -> {
				MessageDescription md = new MessageDescription();
				md.setMessage("Broken DAG: [" + error.getFilename() + "]" + " " + error.getStacktrace());
				errors.add(md);
			});
		});

		return errors;
	}

	@Override
	public List<String> getPermissions(String dagName, String userName) {
		List<String> permissionList = new ArrayList<String>();
		List<Object[]> permissions = dnaProjectRepository.dagPermissionList(dagName, userName);
		for (Object[] objArray : permissions) {
			permissionList.add(objArray[0].toString());
		}
		return permissionList;
	}

}
