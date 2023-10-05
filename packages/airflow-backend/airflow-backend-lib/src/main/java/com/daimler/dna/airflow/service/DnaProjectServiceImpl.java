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
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.dna.airflow.app.main.auth.UserStore;
import com.daimler.dna.airflow.assembler.DnaProjectAssesmbler;
import com.daimler.dna.airflow.assembler.RoleAssembler;
import com.daimler.dna.airflow.assembler.UserAssembler;
import com.daimler.dna.airflow.client.AirflowGitClient;
import com.daimler.dna.airflow.dto.AirflowDagProjectResponseVo;
import com.daimler.dna.airflow.dto.AirflowDagVo;
import com.daimler.dna.airflow.dto.AirflowGITResponse;
import com.daimler.dna.airflow.dto.AirflowProjectResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.dto.AirflowProjectsByUserVO;
import com.daimler.dna.airflow.dto.AirflowRetryDagVo;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.models.CollabInfo;
import com.daimler.dna.airflow.models.Dag;
import com.daimler.dna.airflow.models.DagCollabInfo;
import com.daimler.dna.airflow.models.DagCollabInfoCollection;
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
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private KafkaProducerService kafkaProducer;
	
	private static String DEFAULT_USER_ROLENAME = "User";

	@Override
	public List<AirflowProjectsByUserVO> getAllProjects(int offset, int limit) {
		LOGGER.trace("Processing getAllProjects...");
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		Map<String, AirflowProjectsByUserVO> map = new HashMap<String, AirflowProjectsByUserVO>();
		Map<String, AirflowProjectsByUserVO> map1 = new HashMap<String, AirflowProjectsByUserVO>();
		List<AirflowProjectsByUserVO> list = new ArrayList<AirflowProjectsByUserVO>();
		List<AirflowDagProjectResponseVo> dags = null;
		LOGGER.debug("fetching project details for {} from database.", currentUser.getUsername());
		List<Object[]> result = dnaProjectRepository.findAllProjectsByUserId(currentUser.getUsername());
		List<Object[]> result1 = dnaProjectRepository.findAllCreationStatusProjectsByUserId(currentUser.getUsername(), "_REQUESTED");
		LOGGER.debug("Database fetch successfull {}", currentUser.getUsername());
		for (Object[] obj : result) {
			map.put((String) obj[0], assembler.toVO(obj, currentUser.getUsername(), map));
		}

		for (Object[] obj : result1) {
			map.put((String) obj[0], assembler.toVO1(obj, currentUser.getUsername(), map));
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
		if(projects!=null && !projects.isEmpty()) {
		projects = projects.stream().filter(p -> p.getCreatedBy().equalsIgnoreCase(currentUser.getUsername()))
				.collect(Collectors.toList());
		}
		return (Objects.nonNull(projects) && !projects.isEmpty()) ? assembler.toVO(projects.get(0)) : null;
	}
	

	@Override
	@Transactional
	public ResponseEntity<AirflowProjectResponseWrapperVO> createAirflowProject(AirflowProjectVO airflowProjectVO) {
		AirflowProjectResponseWrapperVO res = new AirflowProjectResponseWrapperVO();
		List<MessageDescription> warnings = new ArrayList<MessageDescription>();
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		airflowProjectVO.setProjectStatus("CREATE_REQUESTED");
		airflowProjectVO.setCreatedBy(currentUser.getUsername());
		List<MessageDescription> validErrors = validateProject(airflowProjectVO);
		List<MessageDescription> errors = new ArrayList<>();
		if (ObjectUtils.isEmpty(validErrors)) {
			errors = airflowGitClient.createAirflowDags(airflowProjectVO, currentUser);
			if (ObjectUtils.isEmpty(errors)) {
				DnaProject savedDnaProject = dnaProjectRepository.save(assembler.toEntity(airflowProjectVO));
				List<String> permissions = new ArrayList<>();
				permissions.add("can_edit");
				permissions.add("can_read");
				currentUser.setPermissions(permissions);
				boolean isCurrentUserExist = userExist(currentUser.getEmail());
				boolean isCurrentRoleExist = roleExist(currentUser.getUsername());
				Role defaultUserRole = findRole(DEFAULT_USER_ROLENAME);
				Role savedCurrentUserRole = isCurrentRoleExist ? findRole(currentUser.getUsername())
						: addRole(currentUser.getUsername());
				User savedCurrentUser = isCurrentUserExist ? findUser(currentUser.getEmail()) : addUser(currentUser);
				if (!isCurrentUserExist && !isCurrentRoleExist && Objects.nonNull(savedCurrentUser)
						&& Objects.nonNull(savedCurrentUserRole)) {
					addUserRoleMapping(savedCurrentUser, savedCurrentUserRole);
					if(defaultUserRole!=null) {
						addUserRoleMapping(savedCurrentUser, savedCurrentUserRole);
					}
				}
				// for each dag
				DagCollabInfoCollection dagsInfoCollection = new DagCollabInfoCollection();
				List<DagCollabInfo> dagsInfo = new ArrayList<>();
				for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
					DagCollabInfo tempDagInfo = new DagCollabInfo();
					tempDagInfo.setDagName(dagVO.getDagName());
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
						}
					}
					if (isDagExist) {
						LOGGER.debug("mapping dag and user to project..");
						addPermissionAndMappedToProject(currentUser, dagVO, savedCurrentUserRole, savedCurrentUser,
								savedDnaProject);
					}
					if (isPermissionCreated) {
						LOGGER.debug("mapping permission to role..");
						addRoleAndPermissionMapping(currentUser, savedCurrentUserRole, dagVO.getDagName());
					} 
					List<CollabInfo> collabs = new ArrayList<>();
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
								if(defaultUserRole!=null) {
									addUserRoleMapping(savedCurrentUser, defaultUserRole);
								}
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
							List<String> collabPermissions = userVO.getPermissions();
							if(collabPermissions==null) {
								collabPermissions = new ArrayList<>();
							}
							CollabInfo tempCollabInfo = new CollabInfo();
							tempCollabInfo.setUsername(userVO.getUsername());
							tempCollabInfo.setEmail(userVO.getEmail());
							tempCollabInfo.setFirstName(userVO.getFirstName());
							tempCollabInfo.setLastName(userVO.getLastName());
							tempCollabInfo.setPermissions(collabPermissions);
							collabs.add(tempCollabInfo);
						}
						tempDagInfo.setCollabs(collabs);
					}
					dagsInfo.add(tempDagInfo);
					if (!isDagExist) {
						dagsInfoCollection.setDagsInfo(dagsInfo);
					}
				}
				if(dagsInfoCollection!=null && dagsInfoCollection.getDagsInfo()!= null && !dagsInfoCollection.getDagsInfo().isEmpty()) {
					LOGGER.info("...Dag still not created hence adding status as CREATE_REQUESTED...");
					savedDnaProject.setProjectStatus("CREATE_REQUESTED");
					String collabsInfoAsString = null;
					try {
						collabsInfoAsString = mapper.writeValueAsString(dagsInfoCollection);
						savedDnaProject.setCollabs(collabsInfoAsString);
					}catch(Exception e) {
						MessageDescription error = new MessageDescription("Failed while saving airflow project information due to unexpected server exception, please retry again");
						res.setData(airflowProjectVO);
						errors.add(error);
						res.setErrors(errors);
						res.setStatus("FAILURE");
						return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}else {
					savedDnaProject.setProjectStatus("CREATED");
				}
				dnaProjectRepository.save(savedDnaProject);
			}
			else {
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

	@Scheduled(cron = "*/30 * * * * *")
	@Transactional
	public void updateAirflowInprogressDagProjectStatus() {
		Map<String, AirflowProjectsByUserVO> map1 = new HashMap<String, AirflowProjectsByUserVO>();
		LOGGER.info("..UpdateAirflowDagProjectStatus Started...");
		List<DnaProject> dnaProjects = dnaProjectRepository.findAll(0, 0);
		List<DnaProject> filteredList = dnaProjects.stream()
				.filter(dnaProject -> (dnaProject.getProjectStatus()!= null && dnaProject.getProjectStatus().contains("REQUESTED")))
				.collect(Collectors.toList());
		Role defaultUserRole = findRole(DEFAULT_USER_ROLENAME);
		for(DnaProject dnaProject : filteredList) {
			List<String> teamMembersIds = new ArrayList<>();
			List<String> teamMembersEmails = new ArrayList<>();
			LOGGER.info("Processing update of project {} from scheduled job", dnaProject.getProjectName());
			String collabsDagsInfo = dnaProject.getCollabs();
			DagCollabInfoCollection dagsInfoCollection = new DagCollabInfoCollection();
			List<DagCollabInfo> pendingDagsInfo = new ArrayList<>();
			try {
				dagsInfoCollection = mapper.readValue(collabsDagsInfo, DagCollabInfoCollection.class);
			}catch(Exception e){
				LOGGER.error("Failed to parse information for Project {} in requested state with exception {}, will retry in next schedule",dnaProject.getProjectName(), e.getMessage());
				return;
			}
			boolean processingPending = true;
			if(dagsInfoCollection!=null) {
				List<DagCollabInfo> dagsInfo = dagsInfoCollection.getDagsInfo();
				if(dagsInfo!=null && !dagsInfo.isEmpty()) {
					for(DagCollabInfo dagInfo : dagsInfo) {
						AirflowDagVo dagVO1 = new AirflowDagVo();
						dagVO1.setDagName(dagInfo.getDagName());
						Boolean isErrorExist = isErrorExistInDag(dagInfo.getDagName());
						Boolean isDagExists = checkDagMenu(dagInfo.getDagName());
						Boolean isPermissionCreated = checkDagPermissionAndViewMenu(dagVO1);
						if (isDagExists) {
							LOGGER.info("Processing update of project {} from scheduled job, dag {} exists", dnaProject.getProjectName(),dagVO1.getDagName());
							AirflowProjectUserVO currentUser = new AirflowProjectUserVO();
							String username = dnaProject.getCreatedBy();
							LOGGER.info("..username...{}", username);
							currentUser.setUsername(username);
							List<String> ownerPermissions = new ArrayList<>();
							ownerPermissions.add("can_read");
							ownerPermissions.add("can_edit");
							currentUser.setPermissions(ownerPermissions);
							Role savedCurrentUserRole = findRole(username);
							List<User> existingUser = userRepository.findbyUniqueLiteral("username", username);
							User savedCurrentUser = existingUser != null && !existingUser.isEmpty() ? existingUser.get(0) : new User();
							teamMembersIds.add(savedCurrentUser.getUsername());
							teamMembersEmails.add(savedCurrentUser.getEmail());
							LOGGER.info("mapping dag and user to project");
							if("CREATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
								addPermissionAndMappedToProject(currentUser, dagVO1, savedCurrentUserRole, savedCurrentUser,
										dnaProject);
								if(isPermissionCreated) {
									addRoleAndPermissionMapping(currentUser, savedCurrentUserRole, "DAG:"+dagVO1.getDagName());
								}
							}
							if("UPDATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
								updatePermissionAndMappedToProject(currentUser, dagVO1, savedCurrentUserRole, savedCurrentUser,
										dnaProject);
								if(isPermissionCreated) {
									deleteRoleAndPermissionMapping(savedCurrentUserRole,"DAG:"+  dagVO1.getDagName());
									addRoleAndPermissionMapping(currentUser, savedCurrentUserRole, "DAG:"+dagVO1.getDagName());
								}
							}
							List<CollabInfo> currentDagCollabs = dagInfo.getCollabs();
							if(currentDagCollabs!=null && !currentDagCollabs.isEmpty()) {
									for(CollabInfo tempCollab : currentDagCollabs) {
										String collabId = tempCollab.getUsername();
										boolean isRoleExist = roleExist(collabId);
										boolean isUserExist = userExist(tempCollab.getEmail());
										Role savedCollabRole = isRoleExist ? findRole(collabId) : addRole(collabId);
										User savedCollabUser = isUserExist ? findUser(tempCollab.getEmail()) : addUserFromCollabInfo(tempCollab);
										if (!isUserExist && !isRoleExist && Objects.nonNull(savedCollabUser)
												&& Objects.nonNull(savedCollabRole)) {
											addUserRoleMapping(savedCollabUser, savedCollabRole);
											if(defaultUserRole!=null) {
												addUserRoleMapping(savedCurrentUser, defaultUserRole);
											}
										}
										LOGGER.debug("User onboarded successfully..{}", collabId);
										AirflowProjectUserVO collabUserVO = new AirflowProjectUserVO();
										collabUserVO.setUsername(collabId);
										List<String> collabPermissions = new ArrayList<>();
										collabPermissions.addAll(tempCollab.getPermissions());
										collabUserVO.setPermissions(collabPermissions);
										teamMembersIds.add(savedCollabUser.getUsername());
										teamMembersEmails.add(savedCollabUser.getEmail());
										if("CREATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
											if (isDagExists) {
												LOGGER.debug("mapping dag and user to project..");
												addPermissionAndMappedToProject(collabUserVO, dagVO1, savedCollabRole, savedCollabUser, dnaProject);
											}
											if (isPermissionCreated) {
												LOGGER.debug("mapping permission to role..");
												addRoleAndPermissionMapping(collabUserVO, savedCollabRole, "DAG:"+dagVO1.getDagName());
											}
										}
										if("UPDATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
											if (isDagExists && !currentUser.getUsername().equalsIgnoreCase(tempCollab.getUsername())) {
												LOGGER.debug("mapping dag and user to project..");
												updatePermissionAndMappedToProject(collabUserVO, dagVO1, savedCollabRole, savedCollabUser,dnaProject);
											}
											if (isPermissionCreated && !currentUser.getUsername().equalsIgnoreCase(tempCollab.getUsername())) {
												deleteRoleAndPermissionMapping(savedCollabRole, "DAG:"+dagVO1.getDagName());
												LOGGER.debug("mapping permission to role..");
												updateRoleAndPermissionMapping(collabUserVO, savedCollabRole, "DAG:"+dagVO1.getDagName(),dnaProject);
											}
										}
									}
								}
							}
							else {
								pendingDagsInfo.add(dagInfo);
							}
						}
						if(pendingDagsInfo!=null && !pendingDagsInfo.isEmpty()) {
							processingPending = true;
						}else {
							processingPending = false;
						}
					}
					else {
						processingPending = false;
					}
				}else {
					processingPending = false;
				}
				if(processingPending) {
						String processedStatus = null;
						if("CREATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())){
							processedStatus = "CREATE_REQUESTED";
						}
						if("UPDATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
							processedStatus = "UPDATE_REQUESTED";
						}
						try {
							dagsInfoCollection = new DagCollabInfoCollection();
							dagsInfoCollection.setDagsInfo(pendingDagsInfo);
							String collabsInfoAsString = mapper.writeValueAsString(dagsInfoCollection);
							updateProject(dnaProject,processedStatus,collabsInfoAsString);
							LOGGER.info("Processed project {} to {}, updating with pending dags for processing",dnaProject.getProjectName(),processedStatus);
						}catch(Exception e){
							LOGGER.error("Failed to parse information after processing with dags still pending for Project {} in requested state with exception {}, will retry in next schedule",dnaProject.getProjectName(), e.getMessage());
							return;
						}
                }else {
	                	String processedStatus = null;
	                	String eventType = "";
	                	String message = "";
						if("CREATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())){
							processedStatus = "CREATED";
							eventType = "Airflow-Create";
							message = "Project " + dnaProject.getProjectName() + " is created successfully.";
						}
						if("UPDATE_REQUESTED".equalsIgnoreCase(dnaProject.getProjectStatus())) {
							processedStatus = "UPDATED";
							eventType = "Airflow-update";
							message = "Project " + dnaProject.getProjectName() + " is updated successfully.";
						}
						try {
							dagsInfoCollection = new DagCollabInfoCollection();
							updateProject(dnaProject,processedStatus,"");
							LOGGER.info("Processed project {} to {}, updating with no dags pending for processing",dnaProject.getProjectName(),processedStatus);
							kafkaProducer.send(eventType, dnaProject.getProjectId(), "", dnaProject.getCreatedBy(), message, true, teamMembersIds, teamMembersEmails, null);
						}catch(Exception e){
							LOGGER.error("Failed to parse information after processing all dags for Project {} in requested state with exception {}, will retry in next schedule",dnaProject.getProjectName(), e.getMessage());
							return;
						}	
				}
		}
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
				permissions.add("can_edit");
				permissions.add("can_read");
				currentUser.setPermissions(permissions);
				boolean isCurrentUserExist = userExist(currentUser.getEmail());
				boolean isCurrentRoleExist = roleExist(currentUser.getUsername());
				Role defaultUserRole = findRole(DEFAULT_USER_ROLENAME);
				Role savedCurrentUserRole = isCurrentRoleExist ? findRole(currentUser.getUsername())
						: addRole(currentUser.getUsername());
				User savedCurrentUser = isCurrentUserExist ? findUser(currentUser.getEmail()) : addUser(currentUser);
				if (!isCurrentUserExist && !isCurrentRoleExist && Objects.nonNull(savedCurrentUser)
						&& Objects.nonNull(savedCurrentUserRole)) {
					addUserRoleMapping(savedCurrentUser, savedCurrentUserRole);
					if(defaultUserRole!=null) {
						addUserRoleMapping(savedCurrentUser, defaultUserRole);
					}
				}
				DagCollabInfoCollection dagsInfoCollection = new DagCollabInfoCollection();
				List<DagCollabInfo> dagsInfo = new ArrayList<>();
				for (AirflowDagVo dagVO : airflowProjectVO.getDags()) {
					DagCollabInfo tempDagInfo = new DagCollabInfo();
					tempDagInfo.setDagName(dagVO.getDagName());
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
						} 
					}

					if (isDagExist) {
						updatePermissionAndMappedToProject(currentUser, dagVO, savedCurrentUserRole, savedCurrentUser,
								existingDnaProject);
					}
					if (isPermissionCreated) {
						deleteRoleAndPermissionMapping(savedCurrentUserRole, "DAG:"+  dagVO.getDagName());
						LOGGER.debug("mapping permission to role..");
						updateRoleAndPermissionMapping(currentUser, savedCurrentUserRole,"DAG:"+ dagVO.getDagName(),
								existingDnaProject);
					} 
					List<CollabInfo> collabs = new ArrayList<>();
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
								if(defaultUserRole!=null) {
									addUserRoleMapping(savedCurrentUser, defaultUserRole);
								}
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
								deleteRoleAndPermissionMapping(savedRole,"DAG:"+  dagVO.getDagName());
								LOGGER.debug("mapping permission to role..");
								updateRoleAndPermissionMapping(userVO, savedRole,"DAG:"+  dagVO.getDagName(),
										existingDnaProject);
							}
							List<String> collabPermissions = userVO.getPermissions();
							if(collabPermissions==null) {
								collabPermissions = new ArrayList<>();
							}
							CollabInfo tempCollabInfo = new CollabInfo();
							tempCollabInfo.setUsername(userVO.getUsername());
							tempCollabInfo.setEmail(userVO.getEmail());
							tempCollabInfo.setFirstName(userVO.getFirstName());
							tempCollabInfo.setLastName(userVO.getLastName());
							tempCollabInfo.setPermissions(collabPermissions);
							collabs.add(tempCollabInfo);
						}
						tempDagInfo.setCollabs(collabs);
					}
					dagsInfo.add(tempDagInfo);
					if (!isDagExist) {
						dagsInfoCollection.setDagsInfo(dagsInfo);
					}
				}
				String currentStatus = "UPDATED";
				String collabsInfoAsString = null;
				if(dagsInfoCollection!=null && dagsInfoCollection.getDagsInfo()!= null && !dagsInfoCollection.getDagsInfo().isEmpty()) {
					LOGGER.info("...Dag still not updated hence adding status as UPDATE_REQUESTED...");
					currentStatus = "UPDATE_REQUESTED";
					try {
						collabsInfoAsString = mapper.writeValueAsString(dagsInfoCollection);
					}catch(Exception e) {
						MessageDescription error = new MessageDescription("Failed while saving airflow project information due to unexpected server exception, please retry again");
						res.setData(airflowProjectVO);
						errors.add(error);
						res.setErrors(errors);
						res.setStatus("FAILURE");
						return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
					LOGGER.debug("updating dna project");
					updateProject(updatedProject,currentStatus,collabsInfoAsString);
				}
			 else {
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
		permissions.add("can_edit");
		permissions.add("can_read");
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

	@Override
	@Transactional
	public ResponseEntity<AirflowProjectResponseWrapperVO> getAirflowDagStatus(String projectId) {
		AirflowProjectResponseWrapperVO res = new AirflowProjectResponseWrapperVO();
		AirflowProjectVO airflowProjectVO = this.getByProjectId(projectId);
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		res.setStatus("SUCCESS");
		res.setErrors(errors);
		res.setWarnings(warnings);
		res.setData(airflowProjectVO);
		return new ResponseEntity<AirflowProjectResponseWrapperVO>(res, HttpStatus.OK);
	}

	private void updateProject(DnaProject updatedProject,String status, String dagsCollabsInfo) {
		LOGGER.debug("updating existing project");
		dnaProjectRepository.updateProject(updatedProject,status,dagsCollabsInfo);
	}

	private List<MessageDescription> validateProjectForUpdate(AirflowProjectVO airflowProjectVO, String projectId) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		DnaProject existingProject = getProjectIfExist(projectId);
		if (existingProject !=null ) {
			if("CREATE_REQUESTED".equalsIgnoreCase(existingProject.getProjectStatus())) {
				LOGGER.debug("Project {} still in create requested state, please wait and update once created successfully.",projectId);
				MessageDescription messageDescription = new MessageDescription();
				messageDescription.setMessage("Project still in create requested state, please wait and update once created successfully.");
				errors.add(messageDescription);
			}
		}
		else{
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
	
	private DnaProject getProjectIfExist(String projectId) {
		List<DnaProject> projects = dnaProjectRepository.findbyUniqueLiteral("projectId", projectId);
		return Objects.nonNull(projects) && !projects.isEmpty() ? projects.get(0) : null;
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
	
	private User addUserFromCollabInfo(CollabInfo collabInfo) {
		User savedUser = new User();
		LOGGER.debug("creating user ..{}", collabInfo.getUsername());
		savedUser = userRepository.save(userAssembler.toEntity(collabInfo));
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
