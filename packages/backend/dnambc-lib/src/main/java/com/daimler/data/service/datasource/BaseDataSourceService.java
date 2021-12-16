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

package com.daimler.data.service.datasource;

import com.daimler.data.assembler.DataSourceAssembler;
import com.daimler.data.db.entities.DataSourceNsql;
import com.daimler.data.db.repo.datasource.DataSourceCustomRepository;
import com.daimler.data.db.repo.datasource.DataSourceRepository;
import com.daimler.data.db.repo.solution.SolutionCustomRepository;
import com.daimler.data.dto.datasource.DataSourceVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseDataSourceService
        extends BaseCommonService<DataSourceVO, DataSourceNsql, String>
        implements DataSourceService {

    @Autowired
    private DataSourceCustomRepository customRepo;
    @Autowired
    private DataSourceRepository jpaRepo;
    @Autowired
    private DataSourceAssembler assembler;

    @Autowired
    private SolutionService solutionService;

    public BaseDataSourceService() {
        super();
    }


    @Transactional
    @Override
    public boolean deleteDataSource(final String id){
        DataSourceNsql dsEntity = jpaRepo.getOne(id);
        String dsName = dsEntity.getData().getName();
        solutionService.deleteTagForEachSolution(dsName, null, SolutionService.TAG_CATEGORY.DS);
        return deleteById(id);
    }
}
