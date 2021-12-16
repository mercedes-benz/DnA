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

import com.daimler.data.db.entities.AlgorithmNsql;
import com.daimler.data.db.jsonb.Algorithm;
import com.daimler.data.dto.algorithm.AlgorithmVO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AlgorithmAssembler implements GenericAssembler<AlgorithmVO, AlgorithmNsql> {

    @Override
    public AlgorithmVO toVo(AlgorithmNsql entity) {
        AlgorithmVO algorithmVO = null;
        if (Objects.nonNull(entity)) {
            algorithmVO = new AlgorithmVO();
            algorithmVO.setId(entity.getId());
            algorithmVO.setName(entity.getData().getName());
        }
        return algorithmVO;
    }

    @Override
    public AlgorithmNsql toEntity(AlgorithmVO vo) {
        AlgorithmNsql algorithmNsql = null;
        if (Objects.nonNull(vo)) {
            algorithmNsql = new AlgorithmNsql();
            Algorithm algorithm = new Algorithm();
            algorithm.setName(vo.getName() );
            algorithmNsql.setData(algorithm);
            if (vo.getId() != null)
                algorithmNsql.setId(vo.getId());
        }
        return algorithmNsql;
    }
}
