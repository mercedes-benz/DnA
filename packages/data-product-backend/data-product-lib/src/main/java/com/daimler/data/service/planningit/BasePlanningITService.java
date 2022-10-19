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

package com.daimler.data.service.planningit;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.PlanningITAssembler;
import com.daimler.data.db.entities.PlanningITNsql;
import com.daimler.data.db.repo.planningit.PlanningITCustomRepository;
import com.daimler.data.db.repo.planningit.PlanningITRepository;
import com.daimler.data.dto.planningit.PlanningITVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BasePlanningITService extends BaseCommonService<PlanningITVO, PlanningITNsql, String>
		implements PlanningITService {

	private static Logger LOGGER = LoggerFactory.getLogger(BasePlanningITService.class);

	@Autowired
	private PlanningITAssembler planningItAssembler;

	@Autowired
	private PlanningITRepository jpaRepository;

	@Autowired
	private PlanningITCustomRepository customRepository;

	public BasePlanningITService() {
		super();
	}

	@Override
	public List<PlanningITVO> getAll() {
		List<PlanningITNsql> entities = jpaRepository.findAll();
		return entities.stream().map(n -> planningItAssembler.toVo(n)).collect(Collectors.toList());
	}
	
	@Transactional
	@Override
	public void bulkInsert(List<PlanningITVO> volist) throws Exception {
		if(volist!=null && !volist.isEmpty()) {
			List<PlanningITNsql> entities = volist.stream().map(n -> planningItAssembler.toEntity(n)).collect(Collectors.toList());
			for(PlanningITNsql entity: entities) {
				jpaRepository.save(entity);
			}
		}
	}

	@Override
	public List<PlanningITVO> getAllWithFilter(String searchTerm) {
		List<PlanningITNsql> entities = customRepository.getAllWithFilters(searchTerm);
		return entities.stream().map(n -> planningItAssembler.toVo(n)).collect(Collectors.toList());
	}


}
