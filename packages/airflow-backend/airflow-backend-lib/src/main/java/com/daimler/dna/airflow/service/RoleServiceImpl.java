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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.dna.airflow.assembler.RoleAssembler;
import com.daimler.dna.airflow.dto.AirflowResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleResponseWrapperVO;
import com.daimler.dna.airflow.dto.AirflowRoleVO;
import com.daimler.dna.airflow.exceptions.GenericMessage;
import com.daimler.dna.airflow.exceptions.MessageDescription;
import com.daimler.dna.airflow.models.Role;
import com.daimler.dna.airflow.repository.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository repo;

	@Autowired
	private RoleAssembler assembler;

	@Override
	public List<AirflowRoleVO> getAllAirflowRoles() {
		// List<Role> roles = repo.findAll(0, 0);
		return null;/* roles.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList()); */
	}

	@Override
	@Transactional
	public ResponseEntity<AirflowRoleResponseWrapperVO> createRole(AirflowRoleVO vo) {
		ResponseEntity<AirflowRoleResponseWrapperVO> responseEntity = null;
		/*
		 * List<Role> existingRole = repo.findbyUniqueLiteral("name", vo.getName()); if
		 * (Objects.isNull(existingRole)) { Role role = assembler.toEntity(vo); try {
		 * Role savedRole = repo.save(role); responseEntity =
		 * getResponse("Role "+savedRole.getName() + " created successfully",
		 * HttpStatus.CREATED);
		 * responseEntity.getBody().addAirflowRoleVOItem(assembler.toVo(savedRole));
		 * responseEntity.getBody().getResponse().setSuccess("Success");
		 * }catch(Exception ex) { responseEntity = getResponse("Failed save "
		 * +vo.getName() + "", HttpStatus.INTERNAL_SERVER_ERROR); }
		 * 
		 * }else { responseEntity = getResponse("role already exist",
		 * HttpStatus.BAD_REQUEST); }
		 */

		return responseEntity;
	}

	private ResponseEntity<AirflowRoleResponseWrapperVO> getResponse(String msg, HttpStatus status) {
		AirflowRoleResponseWrapperVO response = new AirflowRoleResponseWrapperVO();
		GenericMessage gm = new GenericMessage();
		MessageDescription md = new MessageDescription();
		md.setMessage(msg);
		List<MessageDescription> errors = new ArrayList<>();
		errors.add(md);
		gm.setErrors(errors);
		response.setResponse(gm);
		return new ResponseEntity<AirflowRoleResponseWrapperVO>(response, status);
	}
}
