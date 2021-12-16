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

package com.daimler.data.service.projectstatus;

import com.daimler.data.assembler.ProjectStatusAssembler;
import com.daimler.data.db.entities.ProjectStatusNsql;
import com.daimler.data.db.repo.projectstatus.ProjectStatusCustomRepository;
import com.daimler.data.db.repo.projectstatus.ProjectStatusRepository;
import com.daimler.data.dto.projectstatus.ProjectStatusVO;
import com.daimler.data.service.common.BaseCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseProjectStatusService
        extends BaseCommonService<ProjectStatusVO, ProjectStatusNsql, String>
        implements ProjectStatusService {

    @Autowired
    private ProjectStatusCustomRepository customRepo;
    @Autowired
    private ProjectStatusRepository jpaRepo;
    @Autowired
    private ProjectStatusAssembler assembler;

    public BaseProjectStatusService() {
        super();
    }

//    @Autowired
//    public BaseProjectStatusService(ProjectStatusCustomRepository customRepo
//            , ProjectStatusRepository jpaRepo
//            , ProjectStatusAssembler assembler) {
//        super(customRepo, jpaRepo, assembler);
//        this.customRepo = customRepo;
//        this.jpaRepo = jpaRepo;
//        this.assembler = assembler;
//    }

    @Override
    public List<ProjectStatusVO> getAll() {
        List<ProjectStatusVO> voList = super.getAll();
        List<ProjectStatusVO> filteredList = new ArrayList<>();
        filteredList = voList.stream().filter
                (vo -> vo != null && (!"2".equals(vo.getId()) && !"3".equals(vo.getId()))).collect(Collectors.toList());
        return filteredList;
    }


}
