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

package com.daimler.data.service.division;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.daimler.data.assembler.DivisionAssembler;
import com.daimler.data.db.entities.DivisionNsql;
import com.daimler.data.db.repo.division.DivisionCustomRepository;
import com.daimler.data.db.repo.division.DivisionRepository;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.util.ConstantsUtility;

@Service
public class BaseDivisionService
        extends BaseCommonService<DivisionVO, DivisionNsql, String>
        implements DivisionService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDivisionService.class);
	
    @Autowired
    private DivisionCustomRepository customRepo;
    @Autowired
    private DivisionRepository jpaRepo;
    @Autowired
    private DivisionAssembler assembler;

    public BaseDivisionService() {
        super();
    }

//    @Autowired
//    public BaseDivisionService(DivisionCustomRepository customRepo
//            , DivisionRepository jpaRepo
//            , DivisionAssembler assembler) {
//        super(customRepo, jpaRepo, assembler);
//        this.customRepo = customRepo;
//        this.jpaRepo = jpaRepo;
//        this.assembler = assembler;
//    }


    @Override
    public List<SubdivisionVO> getSubDivisionsById(String id) {
        Optional<DivisionNsql> entityOptional = jpaRepo.findById(id);
        DivisionNsql entity = entityOptional != null ? entityOptional.get() : null;
        return assembler.toSubDivisionVoList(entity);
    }

    @Override
	public List<DivisionVO> getDivisionsByIds(List<String> ids) {
		LOGGER.trace("Entering getDivisionsByIds");

		LOGGER.debug("Adding SubdivisionVO object with Empty/None value...");
		SubdivisionVO emptySubDivisionVO = new SubdivisionVO();
		emptySubDivisionVO.setId(ConstantsUtility.EMPTY_VALUE);
		emptySubDivisionVO.setName(ConstantsUtility.NONE_VALUE);

		List<DivisionNsql> entityList = ObjectUtils.isEmpty(ids) ? jpaRepo.findAll() : jpaRepo.findAllById(ids);
		List<DivisionVO> divisionsVO = entityList.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());

		if (!ObjectUtils.isEmpty(divisionsVO)) {
			LOGGER.info("Appending Subdivision object with Empty/None value");
			divisionsVO.get(0).getSubdivisions().add(0, emptySubDivisionVO);
		}
		LOGGER.trace("Returning from getDivisionsByIds");
		return divisionsVO;
	}

}
