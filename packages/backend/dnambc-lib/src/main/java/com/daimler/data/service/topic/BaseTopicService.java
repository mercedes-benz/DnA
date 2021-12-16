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

package com.daimler.data.service.topic;

import com.daimler.data.assembler.TopicAssembler;
import com.daimler.data.db.entities.TopicNsql;
import com.daimler.data.db.repo.topic.TopicCustomRepository;
import com.daimler.data.db.repo.topic.TopicRepository;
import com.daimler.data.dto.topic.TopicVO;
import com.daimler.data.service.common.BaseCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseTopicService
        extends BaseCommonService<TopicVO, TopicNsql, String>
        implements TopicService {

    @Autowired
    private TopicCustomRepository customRepo;
    @Autowired
    private TopicRepository jpaRepo;
    @Autowired
    private TopicAssembler assembler;

    public BaseTopicService() {
        super();
    }

//    @Autowired
//    public BaseTopicService(TopicCustomRepository customRepo
//            , TopicRepository jpaRepo
//            , TopicAssembler assembler) {
//        super(customRepo, jpaRepo, assembler);
//        this.customRepo = customRepo;
//        this.jpaRepo = jpaRepo;
//        this.assembler = assembler;
//    }

}
