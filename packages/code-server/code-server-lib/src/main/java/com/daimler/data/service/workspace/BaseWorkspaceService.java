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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.assembler.WorkspaceAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRepository;
import com.daimler.data.db.repo.workspace.WorkspaceRepository;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.InitializeWorkspaceResponseVO;

@Service
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
	

	public BaseWorkspaceService() {
		super();
	}


	@Override
	public GenericMessage deleteById(String id) {
		Optional<CodeServerWorkspaceNsql> entity = jpaRepo.findById(id);
		boolean flag = entity.isPresent();
		if (flag) {
			jpaRepo.deleteById(id);
		}
		return null;
	}


	@Override
	public InitializeWorkspaceResponseVO create(CodeServerWorkspaceVO vo, String password) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public CodeServerWorkspaceVO getById(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<CodeServerWorkspaceVO> getAll(String userId, int offset, int limit) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public CodeServerWorkspaceVO getByUniqueliteral(String userId, String uniqueLiteral, String value) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public GenericMessage deployWorspace(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
