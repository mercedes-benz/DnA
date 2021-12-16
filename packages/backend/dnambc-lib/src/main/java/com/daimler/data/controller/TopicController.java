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

package com.daimler.data.controller;

import com.daimler.data.api.topic.TopicsApi;
import com.daimler.data.dto.topic.TopicCollection;
import com.daimler.data.dto.topic.TopicVO;
import com.daimler.data.service.topic.TopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "Topic API",tags = {"topics"})
@RequestMapping("/api")
public class TopicController
        implements TopicsApi {


    @Autowired
    private TopicService topicService;


    @Override
    @ApiOperation(value = "Get all available topics.", nickname = "getAll", notes = "Get all topics. This endpoints will be used to Get all valid available topics maintenance records.", response = TopicCollection.class, tags = {"topics",})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully completed fetching all topics", response = TopicCollection.class),
            @ApiResponse(code = 204, message = "Fetch complete, no content found"),
            @ApiResponse(code = 500, message = "Internal error")})
    @RequestMapping(value = "/topics",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<TopicCollection> getAll() {
        final List<TopicVO> topicsVo = topicService.getAll();
        TopicCollection topicCollection = new TopicCollection();
        if (topicsVo != null && topicsVo.size() > 0) {
            topicCollection.addAll(topicsVo);
            return new ResponseEntity<>(topicCollection, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(topicCollection, HttpStatus.NO_CONTENT);
        }
    }


}
