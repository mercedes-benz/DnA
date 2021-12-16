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

package com.daimler.data.assembler;

import com.daimler.data.db.entities.ResultNsql;
import com.daimler.data.db.jsonb.Result;
import com.daimler.data.dto.result.ResultVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ResultAssembler
        implements GenericAssembler<ResultVO, ResultNsql> {

    @Override
    public ResultVO toVo(ResultNsql entity) {
        ResultVO resultVO = null;
        if (Objects.nonNull(entity)) {
            resultVO = new ResultVO();
            resultVO.setId(entity.getId());
            resultVO.setName(entity.getData().getName());
        }
        return resultVO;
    }

    @Override
    public ResultNsql toEntity(ResultVO vo) {
        ResultNsql resultNsql = null;
        if (Objects.nonNull(vo)) {
            resultNsql = new ResultNsql();
            Result result = new Result();
            result.setName(vo.getName() );
            resultNsql.setData(result);
            if (vo.getId() != null)
                resultNsql.setId(vo.getId());
        }
        return resultNsql;
    }
    
    public List<ResultVO> filterOldResults(List<ResultVO> resultsVO){
    	if (resultsVO != null && resultsVO.size() > 0) {
        	String[] oldResults = {"SHAREPOINT","SERVICE"};
        	List<String> resultFilters = Arrays.asList(oldResults);
        	List<ResultVO> newResultsVO = new ArrayList<>();
        	newResultsVO = resultsVO.stream().filter(result -> !resultFilters.contains(result.getName())).collect(Collectors.toList());
        	ResultVO miscResult = new ResultVO();
        	miscResult.setId("2");
        	miscResult.setName("Miscellaneous");
        	newResultsVO.add(miscResult);
        	return newResultsVO;
    	}
    	else return resultsVO;
    }

}
