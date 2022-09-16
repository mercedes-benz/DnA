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

package com.daimler.data.service.department;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.data.assembler.DepartmentAssembler;
import com.daimler.data.db.entities.DepartmentNsql;
import com.daimler.data.db.repo.department.DepartmentCustomRepository;
import com.daimler.data.db.repo.department.DepartmentRepository;
import com.daimler.data.dto.department.DepartmentCollection;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BaseDepartmentService extends BaseCommonService<DepartmentVO, DepartmentNsql, String>
		implements DepartmentService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDepartmentService.class);

	@Autowired
	private DepartmentCustomRepository customRepo;

	@Autowired
	private DepartmentRepository jpaRepo;

	@Autowired
	private DepartmentAssembler departmentAssembler;

	public BaseDepartmentService() {
		super();
	}

	@Override
	public ResponseEntity<DepartmentCollection> getAllDepartments() {
		DepartmentCollection departmentCollection = new DepartmentCollection();
		try {
			List<DepartmentVO> departments = super.getAll();
			LOGGER.debug("Departments fetched successfully");
			if (!ObjectUtils.isEmpty(departments)) {
				departmentCollection.setData(departments);
				return new ResponseEntity<>(departmentCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(departmentCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch departments with exception {} ", e.getMessage());
			throw e;
		}
	}
}
