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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.client.dataiku.DataikuClient;
import com.daimler.data.db.entities.DataikuNsql;
import com.daimler.data.db.entities.NotebookNsql;
import com.daimler.data.db.jsonb.Dataiku;
import com.daimler.data.db.repo.dataiku.DataikuCustomRepository;
import com.daimler.data.db.repo.dataiku.DataikuRepository;
import com.daimler.data.dto.dataiku.DataikuPermission;
import com.daimler.data.dto.dataiku.DataikuProjectVO;
import com.daimler.data.dto.dataiku.DataikuUserRole;
import com.daimler.data.dto.dataiku.Permission;

@Service
public class BaseDataikuService implements DataikuService {

	private Logger LOGGER = LoggerFactory.getLogger(BaseDataikuService.class);

	@Autowired
	private DataikuClient dataikuClient;

	@Value("${dataiku.production.adminGroup}")
	private String prodAdminGroup;

	@Value("${dataiku.production.onPremiseAdminGroup}")
	private String onPremiseAdminGroup;

	@Value("${dataiku.training.adminGroup}")
	private String trainingAdminGroup;

	@Autowired
	private DataikuCustomRepository customRepo;

	@Autowired
	private DataikuRepository jpaRepo;

	private static final String DATAIKU_ADMINISTRATOR = "DATAIKU-ADMINISTRATOR";
	private static final String ADMINISTRATOR = "ADMINISTRATOR";
	private static final String CONTRIBUTOR = "CONTRIBUTOR";
	private static final String READ_ONLY = "READ-ONLY";

	public BaseDataikuService() {
		super();
	}

	/**
	 * <p>
	 * getAll DataikuProjects identified for user.
	 * </p>
	 * 
	 * @param userId
	 * @param live
	 * @param cloudProfile
	 * @return List<DataikuProjectVO>
	 */
	@Override
	@Transactional
	public List<DataikuProjectVO> getAllDataikuProjects(String userId, Boolean live, String cloudProfile) {
		List<DataikuProjectVO> res = null;
		LOGGER.debug("Calling dataiku for user Role of userId {} ", userId);
		Optional<DataikuUserRole> userRole = dataikuClient.getDataikuUserRole(userId, live, cloudProfile);
		LOGGER.debug("Calling dataiku for projects...");
		Optional<List<DataikuProjectVO>> projects = dataikuClient.getAllDataikuProjects(live, cloudProfile);
		if (projects.isPresent() && userRole.isPresent()) {
			if (live) {
				LOGGER.debug("Processing for production data..");
				if (userRole.get().getGroups().contains(prodAdminGroup) || userRole.get().getGroups().contains(onPremiseAdminGroup)) {
					LOGGER.info("Admin: Returning all projects");
					res = new ArrayList<DataikuProjectVO>();
					res = projects.get().stream().peek(project -> project.setRole(DATAIKU_ADMINISTRATOR))
							.collect(Collectors.toList());
				} else {
					LOGGER.info("Normal user: Checking for permission");
					res = new ArrayList<DataikuProjectVO>();
					for (DataikuProjectVO project : projects.get()) {
						if (project.getOwnerLogin().contains(userId.toLowerCase())) {
							LOGGER.debug("Owner of the project");
							project.setRole(ADMINISTRATOR);
							res.add(project);
						} else {
							LOGGER.debug("Fetching permission..");
							Optional<DataikuPermission> projectPermission = dataikuClient
									.getDataikuProjectPermission(project.getProjectKey(), live, cloudProfile);
							if (projectPermission.isPresent()
									&& !ObjectUtils.isEmpty(projectPermission.get().getPermissions())) {
								List<Permission> permissions = projectPermission.get().getPermissions().stream()
										.filter(n -> (StringUtils.hasText(n.getGroup())
												&& userRole.get().getGroups().contains(n.getGroup())))
										.collect(Collectors.toList());
								if (!ObjectUtils.isEmpty(permissions)) {
									LOGGER.debug("Setting role..");
									if (permissions.get(0).getGroup().contains(ADMINISTRATOR)) {
										project.setRole(ADMINISTRATOR);
									} else if (permissions.get(0).getGroup().contains(CONTRIBUTOR)) {
										project.setRole(CONTRIBUTOR);
									} else if (permissions.get(0).getGroup().contains(READ_ONLY)) {
										project.setRole(READ_ONLY);
									}
									res.add(project);
								}
							}
						}
					}
				}
			} else {
				LOGGER.debug("Processing for training data..");
				if (userRole.get().getGroups().contains(trainingAdminGroup)) {
					LOGGER.info("Admin: Returning all projects");
					res = new ArrayList<DataikuProjectVO>();
					res = projects.get();
				} else {
					LOGGER.info("Normal user: Checking for permission");
					res = new ArrayList<DataikuProjectVO>();
					res = projects.get().stream().filter(n -> (StringUtils.hasText(n.getOwnerLogin())
							&& n.getOwnerLogin().contains(userId.toLowerCase()))).collect(Collectors.toList());
				}
			}
		}

		List<DataikuNsql> entities = jpaRepo.findAll();
		if (res != null && !res.isEmpty()) {
			res = res.stream().map(project -> {

				String key = project.getProjectKey();
				if (key != null) {
					DataikuNsql matchingEntity = entities.stream()
							.filter(n -> key.equalsIgnoreCase(n.getData().getProjectKey())).findAny().orElse(null);
					if (matchingEntity != null && matchingEntity.getData() != null)
						project.setSolutionId(matchingEntity.getData().getSolutionId());
				}
				return project;
			}).collect(Collectors.toList());
		}
		return res;
	}

	/**
	 * get Dataiku project by given projectKey
	 * 
	 * @param projectKey
	 * @return DataikuProjectVO
	 */
	@Override
	@Transactional
	public DataikuProjectVO getByProjectKey(String projectKey, Boolean live, String cloudProfile) {
		return dataikuClient.getDataikuProject(projectKey, live, cloudProfile).get();
	}

	@Override
	@Transactional
	public void updateSolutionIdOfDataIkuProjectId(String dataikuProjectId, String solutionId) {
//		DataikuNsql dataikuEntity = customRepo.findbyUniqueLiteral("projectKey", dataikuProjectId);
//		if(dataikuEntity!= null && dataikuEntity.getData()!=null) {
//			Dataiku jsonb= dataikuEntity.getData(); 
//			jsonb.setSolutionId(solutionId);
//			dataikuEntity.setData(jsonb);
//			customRepo.update(dataikuEntity);
//		}else {
//			dataikuEntity = new DataikuNsql();
//			Dataiku jsonb= new Dataiku();
//			jsonb.setProjectKey(dataikuProjectId);
//			jsonb.setSolutionId(solutionId);
//			dataikuEntity.setData(jsonb);
//			jpaRepo.save(dataikuEntity);
//		}

		if (solutionId != null) {
			DataikuNsql dataikuEntity = customRepo.findbyUniqueLiteral("solutionId", solutionId);
			if (dataikuEntity != null && dataikuEntity.getData() != null) {
				if (dataikuProjectId != null && !"".equals(dataikuProjectId)) {
					String preProjectId = dataikuEntity.getId();
					if (!preProjectId.equalsIgnoreCase(dataikuProjectId)) {
						Dataiku jsonb = dataikuEntity.getData();
						jsonb.setSolutionId(null);
						dataikuEntity.setData(jsonb);
						customRepo.update(dataikuEntity);
						LOGGER.debug("linked dataiku project {} to solution {}", preProjectId, solutionId);
						// unlinked
						dataikuEntity = customRepo.findbyUniqueLiteral("projectKey", dataikuProjectId);
						if (dataikuEntity != null && dataikuEntity.getData() != null) {
							jsonb = dataikuEntity.getData();
							jsonb.setSolutionId(solutionId);
							dataikuEntity.setData(jsonb);
							customRepo.update(dataikuEntity);
							// linked
							LOGGER.debug("linked dataiku project {} to solution {}", dataikuProjectId, solutionId);
						} else {
							dataikuEntity = new DataikuNsql();
							jsonb = new Dataiku();
							jsonb.setProjectKey(dataikuProjectId);
							jsonb.setSolutionId(solutionId);
							dataikuEntity.setData(jsonb);
							jpaRepo.save(dataikuEntity);
							// initial link
							LOGGER.debug("linked dataiku project {} to solution {}", dataikuProjectId, solutionId);
						}
					}
				} else {
					Dataiku jsonb = dataikuEntity.getData();
					jsonb.setSolutionId(null);
					dataikuEntity.setData(jsonb);
					customRepo.update(dataikuEntity);
					LOGGER.debug("unlinked dataiku project {} from solution {}", dataikuProjectId, solutionId);
					// unlinked
				}
			} else {
				if (dataikuProjectId != null && !"".equals(dataikuProjectId)) {
					dataikuEntity = customRepo.findbyUniqueLiteral("projectKey", dataikuProjectId);
					if (dataikuEntity != null && dataikuEntity.getData() != null) {
						Dataiku jsonb = dataikuEntity.getData();
						jsonb.setSolutionId(solutionId);
						dataikuEntity.setData(jsonb);
						customRepo.update(dataikuEntity);
						LOGGER.debug("linked dataiku project {} to solution {}", dataikuProjectId, solutionId);
						// linked
					} else {
						dataikuEntity = new DataikuNsql();
						Dataiku jsonb = new Dataiku();
						jsonb.setProjectKey(dataikuProjectId);
						jsonb.setSolutionId(solutionId);
						dataikuEntity.setData(jsonb);
						jpaRepo.save(dataikuEntity);
						LOGGER.debug("linked dataiku project {} to solution {}", dataikuProjectId, solutionId);
						// initial link
					}
				}
			}

		} else {
			if (dataikuProjectId != null && !"".equals(dataikuProjectId)) {
				DataikuNsql dataikuEntity = customRepo.findbyUniqueLiteral("projectKey", dataikuProjectId);
				if (dataikuEntity != null && dataikuEntity.getData() != null) {
					Dataiku jsonb = dataikuEntity.getData();
					jsonb.setSolutionId(solutionId);
					dataikuEntity.setData(jsonb);
					customRepo.update(dataikuEntity);
					LOGGER.debug("unlinked dataiku project {} from solution, on solution delete", dataikuProjectId);
					// unlinked
				}
			}
		}
	}

}
