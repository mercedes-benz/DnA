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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.db.entities.DepartmentNsql;
import com.daimler.data.db.repo.department.DepartmentRepository;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;

@Service
public class BaseDepartmentService extends BaseCommonService<DepartmentVO, DepartmentNsql, String>
		implements DepartmentService {
	
	@Autowired
	private DepartmentRepository jpRepo;
	
	@Autowired
	private SolutionService solutionService;

	public BaseDepartmentService() {
		super();
	}
	
	@Transactional
	@Override
	public boolean deleteDepartment(final String departmentIdToDelete) {
		DepartmentNsql departmentEntity = jpRepo.getOne(departmentIdToDelete);		
			String departmentName = departmentEntity.getData().getName();
			solutionService.deleteTagForEachSolution(departmentName, null, SolutionService.TAG_CATEGORY.DEPARTMENT);
			return deleteById(departmentIdToDelete);		
				
	}
}
