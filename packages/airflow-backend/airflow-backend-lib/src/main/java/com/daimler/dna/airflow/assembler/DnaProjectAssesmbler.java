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

package com.daimler.dna.airflow.assembler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.daimler.dna.airflow.client.AirflowGitClient;
import com.daimler.dna.airflow.dto.AirflowDagProjectResponseVo;
import com.daimler.dna.airflow.dto.AirflowDagVo;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowProjectVO;
import com.daimler.dna.airflow.dto.AirflowProjectsByUserVO;
import com.daimler.dna.airflow.models.DnaProject;
import com.daimler.dna.airflow.models.DnaProjectAndUserMapping;
import com.daimler.dna.airflow.models.DnaProjectUserAndDagMapping;
import com.daimler.dna.airflow.models.Permission;
import com.daimler.dna.airflow.models.PermissionAndRoleMapping;
import com.daimler.dna.airflow.models.PermissionAndViewMenuMapping;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.models.User;
import com.daimler.dna.airflow.models.UserRoleMapping;
import com.daimler.dna.airflow.models.ViewMenu;

@Component
public class DnaProjectAssesmbler {
	private Logger log = LoggerFactory.getLogger(DnaProjectAssesmbler.class);

	@Autowired
	private AirflowGitClient airflowGitClient;

	public AirflowProjectVO toVO(DnaProject dnaProject) {
		log.trace("Started assembling from entity to model");
		final AirflowProjectVO vo = new AirflowProjectVO();
		Set<String> dagSet = new HashSet<String>();
		Map<String, AirflowDagVo> dagMap = new HashMap<>();
		if (Objects.nonNull(dnaProject)) {
			BeanUtils.copyProperties(dnaProject, vo);
			AirflowDagVo dagVO = null;
			log.debug("mapping user to aiflow project...");
			for (DnaProjectAndUserMapping mapping : dnaProject.getDnaProjectUserMappings()) {
				DnaProjectUserAndDagMapping userDagMapping = mapping.getDnaProjectUserAndDagMapping();

				if (dagMap.containsKey(userDagMapping.getDag().getDagId())) {
					dagVO = dagMap.get(userDagMapping.getDag().getDagId());
				} else {
					dagVO = new AirflowDagVo();
				}
				dagVO.setDagName(userDagMapping.getDag().getDagId());
				dagVO.setActive(userDagMapping.getDag().getIsActive());
				dagSet.add(userDagMapping.getDag().getDagId());
				User user = userDagMapping.getUser();
				log.debug("processing mapping for user {}", user.getId());
				if (!dnaProject.getCreatedBy().equalsIgnoreCase(user.getUsername())) {
					AirflowProjectUserVO airflowProjectUserVO = new AirflowProjectUserVO();
					BeanUtils.copyProperties(user, airflowProjectUserVO);
					for (UserRoleMapping roleMapping : user.getRoleMaping()) {
						Role role = roleMapping.getRole();
						log.debug("mapping for tole {} to user {}", role.getName(), user.getId());
						for (PermissionAndRoleMapping permissionMapping : role.getPermissionAndRoleMapping()) {
							PermissionAndViewMenuMapping permissionAndViewMenuMapping = permissionMapping
									.getPermissionView();
							Permission permission = permissionAndViewMenuMapping.getPermission();
							ViewMenu viewMenu = permissionAndViewMenuMapping.getViewMenu();
							if (viewMenu.getName().equalsIgnoreCase(userDagMapping.getDag().getDagId())) {
								airflowProjectUserVO.addPermissionsItem(permission.getName());
							}
						}
					}
					dagVO.addCollaboratorsItem(airflowProjectUserVO);
				}
				
				dagMap.put(userDagMapping.getDag().getDagId(), dagVO);
				
			}
			dagMap.entrySet().forEach(x -> vo.addDagsItem(x.getValue()));
			log.debug("fetching dag content from GIT..");
			Map<String, String> dagsContentMap = airflowGitClient.getDagContentByIds(dagSet);
			if (!ObjectUtils.isEmpty(dagsContentMap)) {
				List<AirflowDagVo> listDags = vo.getDags().stream().map(x -> {
					log.debug("fetching content for {}", x.getDagName());
					x.setDagContent(dagsContentMap.get(x.getDagName()));
					return x;
				}).collect(Collectors.toList());
				vo.setDags(listDags);
			}
		}
		vo.setIsOwner(true);
		log.trace("Successfully assembled entity to model");
		return vo;
	}

	public DnaProject toEntity(AirflowProjectVO vo) {
		DnaProject dnaProject = null;
		if (Objects.nonNull(vo)) {
			dnaProject = new DnaProject();
			BeanUtils.copyProperties(vo, dnaProject);
		}
		return dnaProject;
	}

	public AirflowProjectsByUserVO toVO(Object[] obj, String currentUser, Map<String, AirflowProjectsByUserVO> map) {
		log.trace("Started assembling all aiflow project per user ....");
		AirflowProjectsByUserVO vo = null;
		AirflowDagProjectResponseVo dagsItem = null;
		if (Objects.nonNull(obj)) {
			if (map.get((String) obj[0]) != null) {
				vo = map.get((String) obj[0]);

			} else {
				vo = new AirflowProjectsByUserVO();

			}
			dagsItem = new AirflowDagProjectResponseVo();
			vo.setProjectId((String) obj[0]);
			vo.setProjectName((String) obj[4]);
			vo.setProjectDescription((String) obj[5]);
			vo.setCreatedBy((String) obj[1]);
			vo.setIsOwner(currentUser.equalsIgnoreCase((String) obj[1]));
			vo.setProjectStatus((String) obj[6]);
			dagsItem.setDagName((String) obj[2]);
			dagsItem.addPermissionsItem((String) obj[3]);
			vo.addDagsItem(dagsItem);
		}
		log.trace("Successfully assembled all aiflow project per user.");
		return vo;
	}

	public AirflowProjectsByUserVO toVO1(Object[] obj, String currentUser, Map<String, AirflowProjectsByUserVO> map) {
		log.trace("Started assembling all aiflow project per user ....");
		AirflowProjectsByUserVO vo = null;
		AirflowDagProjectResponseVo dagsItem = null;
		if (Objects.nonNull(obj)) {
			if (map.get((String) obj[0]) != null) {
				vo = map.get((String) obj[0]);

			} else {
				vo = new AirflowProjectsByUserVO();
			}
			dagsItem = new AirflowDagProjectResponseVo();
			vo.setProjectId((String) obj[0]);
			vo.setProjectName((String) obj[2]);
			vo.setProjectDescription((String) obj[3]);
			vo.setCreatedBy((String) obj[1]);
			vo.setProjectStatus((String) obj[4]);
			vo.setIsOwner(currentUser.equalsIgnoreCase((String) obj[1]));
			String dagName = (String) obj[0] + "_DAG_1";
			dagsItem.setDagName(dagName);
			dagsItem.addPermissionsItem("can_read");
			String collabsDetails = (String) obj[5];
			if(collabsDetails !=null ) {
			String[] individualCollabDetails = collabsDetails.split(",");
			Optional<String> collab = Arrays.stream(individualCollabDetails).filter(x -> x.contains(currentUser)).findFirst();
			if(collab.isPresent() && collab.get().split("_").length >1)
				dagsItem.addPermissionsItem("can_edit");
			}
			vo.addDagsItem(dagsItem);
		}
		log.trace("Successfully assembled all aiflow project per user.");
		return vo;
	}

}
