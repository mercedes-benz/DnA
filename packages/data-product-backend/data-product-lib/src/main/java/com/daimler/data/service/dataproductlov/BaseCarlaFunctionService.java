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

package com.daimler.data.service.dataproductlov;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.data.assembler.CarlaFunctionAssembler;
import com.daimler.data.db.entities.CarlaFunctionNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.repo.dataproductlov.CarlaFunctionCustomRepository;
import com.daimler.data.dto.dataproductlov.CarlaFunctionVO;
import com.daimler.data.service.common.BaseCommonService;

@Service
public class BaseCarlaFunctionService extends BaseCommonService<CarlaFunctionVO, CarlaFunctionNsql, String>
		implements CarlaFunctionService {
	

	@Autowired
	private CarlaFunctionCustomRepository customRepo;
	
	@Autowired
	private CarlaFunctionAssembler carlaFunctionAssembler;
	private static Logger LOGGER = LoggerFactory.getLogger(BaseCarlaFunctionService.class);		

	public List<CarlaFunctionVO> getCarlafunctions( int offset, int limit, String sortOrder) {
		List<CarlaFunctionNsql> carlaFunctionEntities = new ArrayList<>();
		if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
			carlaFunctionEntities = customRepo.findAllSortyByUniqueLiteral(limit, offset, "name",CommonDataRepositoryImpl.SORT_TYPE.ASC);
		}
		if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
			carlaFunctionEntities = customRepo.findAllSortyByUniqueLiteral(limit, offset, "name",CommonDataRepositoryImpl.SORT_TYPE.DESC);
		}
		LOGGER.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(carlaFunctionEntities)) {
			return carlaFunctionEntities.stream().map(n -> carlaFunctionAssembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}
	}

	
}
