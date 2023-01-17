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

import com.daimler.data.assembler.FrontEndToolsAssembler;
import com.daimler.data.db.entities.FrontEndToolsNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.db.repo.dataproductlov.FrontEndToolCustomRepository;
import com.daimler.data.dto.dataproductlov.FrontEndToolsVO;
import com.daimler.data.service.common.BaseCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BaseFrontEndToolsService extends BaseCommonService<FrontEndToolsVO, FrontEndToolsNsql, String> implements FrontEndToolsService {


    @Autowired
    private FrontEndToolCustomRepository customRepo;

    @Autowired
    private FrontEndToolsAssembler platformAssembler;
    private static Logger LOGGER = LoggerFactory.getLogger(BaseFrontEndToolsService.class);

    public List<FrontEndToolsVO> getFrontEndTools(int offset, int limit, String sortOrder) {
        List<FrontEndToolsNsql> frontEndToolsEntities = new ArrayList<>();
        if (sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
            frontEndToolsEntities = customRepo.findAllSortyByUniqueLiteral(limit, offset, "name", CommonDataRepositoryImpl.SORT_TYPE.ASC);
        }
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            frontEndToolsEntities = customRepo.findAllSortyByUniqueLiteral(limit, offset, "name", CommonDataRepositoryImpl.SORT_TYPE.DESC);
        }
        LOGGER.info("Success from get information from table.");
        if (!ObjectUtils.isEmpty(frontEndToolsEntities)) {
            return frontEndToolsEntities.stream().map(n -> platformAssembler.toVo(n)).toList();
        } else {
            return new ArrayList<>();
        }
    }


}
