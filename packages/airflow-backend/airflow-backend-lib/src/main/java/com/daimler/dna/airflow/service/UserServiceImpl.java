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

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.daimler.dna.airflow.app.main.auth.UserStore;
import com.daimler.dna.airflow.assembler.RoleAssembler;
import com.daimler.dna.airflow.assembler.UserAssembler;
import com.daimler.dna.airflow.dto.AirflowProjectUserVO;
import com.daimler.dna.airflow.dto.AirflowResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.dto.AirflowUserVO;
import com.daimler.dna.airflow.dto.CreatedByVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.models.User;
import com.daimler.dna.airflow.models.UserRoleMapping;
import com.daimler.dna.airflow.repository.RoleRepository;
import com.daimler.dna.airflow.repository.UserRepository;
import com.daimler.dna.airflow.repository.UserRoleMappingRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserRoleMappingRepository userRoleMappingRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private UserAssembler userAssembler;

	@Autowired
	private RoleAssembler roleAssembler;

	@Autowired
	private UserStore userStore;

	/*
	 * @Override public List<AirflowUserVO> getAllAirflowUsers() {
	 * List<AirflowUserVO> users = new ArrayList<>(); List<User> listUser =
	 * userRepo.findAll(0, 0); for(User user : listUser) { AirflowUserVO userVo =
	 * userAssembler.toVo(user); List<UserRoleMapping> mappings =
	 * userRoleMappingRepo.findbyUniqueLiteral("userId", user.getId());
	 * for(UserRoleMapping mapping : mappings) { Role role =
	 * roleRepo.findbyUniqueLiteral("id", mapping.getRoleId()).get(0); AirflowRoleVO
	 * roleVo = roleAssembler.toVo(role); userVo.addRolesItem(roleVo); }
	 * users.add(userVo); } return users; }
	 */

	@Override
	public List<AirflowUserVO> getAllAirflowUsers() {
		List<User> listUser = userRepo.findAll(0, 0);
		return listUser.stream().map(n -> userAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ResponseEntity<AirflowResponseWrapperVO> createUser(AirflowUserVO vo) {
		ResponseEntity<AirflowResponseWrapperVO> responseEntity = null;
		AirflowProjectUserVO currentUser = this.userStore.getVO();
		List<User> existingUser = userRepo.findbyUniqueLiteral("email", vo.getEmail());
		if (Objects.isNull(existingUser)) {
			User user = userAssembler.toEntity(vo);
			try {
				User savedUser = userRepo.save(user);
				responseEntity = getResponse(savedUser.getUsername() + " created successfully", HttpStatus.CREATED);
				responseEntity.getBody().addAirflowUserVOItem(userAssembler.toVo(savedUser));
				responseEntity.getBody().getResponse().setSuccess("Success");
			} catch (Exception ex) {
				responseEntity = getResponse("Failed save " + user.getUsername() + "",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			responseEntity = getResponse("user already exist", HttpStatus.BAD_REQUEST);
		}
		return responseEntity;
	}

	private ResponseEntity<AirflowResponseWrapperVO> getResponse(String msg, HttpStatus status) {
		AirflowResponseWrapperVO response = new AirflowResponseWrapperVO();
		GenericMessage gm = new GenericMessage();
		MessageDescription md = new MessageDescription();
		md.setMessage(msg);
		List<MessageDescription> errors = new ArrayList<>();
		errors.add(md);
		gm.setErrors(errors);
		response.setResponse(gm);
		return new ResponseEntity<AirflowResponseWrapperVO>(response, status);
	}

}
