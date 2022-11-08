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

package com.daimler.data.service.divisionAudit;

import com.daimler.data.assembler.DivisionAuditAssembler;
import com.daimler.data.db.entities.DivisionAuditNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.repo.divisionAudit.DivisionAuditCustomRepository;
import com.daimler.data.dto.divisionAudit.DivisionAuditVO;
import com.daimler.data.service.common.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class BaseDivisionAuditService extends BaseCommonService<DivisionAuditVO, DivisionAuditNsql, String>
		implements DivisionAuditService {

	@Autowired
	private DivisionAuditCustomRepository customRepo;

	@Autowired
	private DivisionAuditAssembler auditAssembler;

	
	private static Logger LOGGER = LoggerFactory.getLogger(BaseDivisionAuditService.class);

	public BaseDivisionAuditService() {
		super();
	}


	@Override
	public List<DivisionAuditVO> getDivisionAudits(int offset, int limit) {
		// TODO Auto-generated method stub
		List<DivisionAuditNsql> auditInfoEntities = customRepo.findAllSortyByUniqueLiteral(limit, offset, "createdOn",CommonDataRepositoryImpl.SORT_TYPE.DESC);
		LOGGER.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(auditInfoEntities)) {
			return auditInfoEntities.stream().map(n -> auditAssembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<DivisionAuditVO> getAuditsByDivisionId(String divisionId,int offset, int limit) {
		// TODO Auto-generated method stub
		LOGGER.info("Fetching user information from table for getAuditsByDivisionId.");
		List<DivisionAuditNsql> auditInfoEntities = customRepo.getAuditsByDivisionId(divisionId, offset, limit);
		LOGGER.info("Success from get information from table.");
		if (!ObjectUtils.isEmpty(auditInfoEntities)) {
			return auditInfoEntities.stream().map(n -> auditAssembler.toVo(n)).toList();
		} else {
			return new ArrayList<>();
		}		
	}


	@Override
	public Long getAuditCount(String divisionId) {
		// TODO Auto-generated method stub
		return customRepo.getAuditCount(divisionId);
	}
	
	
	}


	


