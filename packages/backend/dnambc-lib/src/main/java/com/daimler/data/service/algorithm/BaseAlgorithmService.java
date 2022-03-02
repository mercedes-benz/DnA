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

package com.daimler.data.service.algorithm;

import com.daimler.data.assembler.AlgorithmAssembler;
import com.daimler.data.db.entities.AlgorithmNsql;
import com.daimler.data.db.repo.algorithm.AlgorithmCustomRepository;
import com.daimler.data.db.repo.algorithm.AlgorithmRepository;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaseAlgorithmService extends BaseCommonService<AlgorithmVO, AlgorithmNsql, String>
		implements AlgorithmService {

	@Autowired
	private AlgorithmCustomRepository customRepo;
	@Autowired
	private AlgorithmRepository jpaRepo;
	@Autowired
	private AlgorithmAssembler algoAssembler;
	@Autowired
	private SolutionService solutionService;

	public BaseAlgorithmService() {
		super();
	}

	@Override
	public boolean deleteAlgorithm(String id) {
		AlgorithmNsql algorithmNsql = jpaRepo.getOne(id);
		String name = algorithmNsql.getData().getName();
		log.debug("Calling solutionService deleteTagForEachSolution to delete cascading refences to algorithm {}", id);
		solutionService.deleteTagForEachSolution(name, null, SolutionService.TAG_CATEGORY.ALGO);
		return deleteById(id);
	}
}
