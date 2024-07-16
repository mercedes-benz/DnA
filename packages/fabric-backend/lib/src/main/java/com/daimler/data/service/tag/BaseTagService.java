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

 package com.daimler.data.service.tag;

 import com.daimler.data.assembler.TagAssembler;
 import com.daimler.data.db.entities.TagNsql;
 import com.daimler.data.db.repo.tag.TagCustomRepository;
 import com.daimler.data.db.repo.tag.TagRepository;
 import com.daimler.data.dto.tag.TagVO;
 import com.daimler.data.service.common.BaseCommonService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 @Service
 public class BaseTagService extends BaseCommonService<TagVO, TagNsql, String> implements TagService {
 
     @Autowired
     private TagCustomRepository customRepo;
     @Autowired
     private TagRepository jpaRepo;
     //@Autowired
     //private TagAssembler tagAssembler;
 
    // @Autowired
    // private SolutionService solutionService;
 
     public BaseTagService() {
         super();
     }
 
    //  @Transactional
    //  @Override
    //  public boolean deleteTag(final String tagIdToDelete) {
    //      TagNsql tagEntity = jpaRepo.getOne(tagIdToDelete);
    //      String tagName = tagEntity.getData().getName();
    //      solutionService.deleteTagForEachSolution(tagName, null, SolutionService.TAG_CATEGORY.TAG);

    //      return deleteById(tagIdToDelete);
    //  }
 
 }
 