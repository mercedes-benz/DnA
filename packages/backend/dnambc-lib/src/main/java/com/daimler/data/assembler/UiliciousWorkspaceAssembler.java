
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

import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.dto.uilicious.UiliciousWorkspaceVO;
import org.springframework.stereotype.Component;
import com.daimler.data.dto.UiliciousCreationDTO;
import com.daimler.data.db.jsonb.UiliciousWorkspace;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import com.daimler.data.db.jsonb.LeanGovernance;
import com.daimler.data.dto.uilicious.LeanGovernanceVO;

@Component
public class UiliciousWorkspaceAssembler implements GenericAssembler<UiliciousCreationDTO, UiliciousWorkspaceNsql> {

    public UiliciousCreationDTO toVo(UiliciousWorkspaceNsql entity) {
        UiliciousCreationDTO vo = null;
        if (Objects.nonNull(entity)) {
            vo = new UiliciousCreationDTO();
            vo.setAccountId(entity.getData().getAccountId());
            vo.setCreatedBy(entity.getData().getCreatedBy());
            if (entity.getData().getLeanGovernance() != null) {
                LeanGovernanceVO leanGovernanceVO = new LeanGovernanceVO();
                BeanUtils.copyProperties(entity.getData().getLeanGovernance(), leanGovernanceVO);
                vo.setLeanGovernance(leanGovernanceVO);
            }
            vo.setId(entity.getId());

        }
        return vo;
    }

    public UiliciousWorkspaceNsql toEntity(UiliciousCreationDTO vo) {
        UiliciousWorkspaceNsql entity = null;
        if (Objects.nonNull(vo)) {
            entity = new UiliciousWorkspaceNsql();
            UiliciousWorkspace data = new UiliciousWorkspace();
            data.setAccountId(vo.getAccountId());
            data.setCreatedBy(vo.getCreatedBy());

            LeanGovernance leanGovernanceEntity = new LeanGovernance();
            if (vo.getLeanGovernance() != null) {
                BeanUtils.copyProperties(vo.getLeanGovernance(), leanGovernanceEntity);
                data.setLeanGovernance(leanGovernanceEntity);
            }
            entity.setData(data);
            if (vo.getId() != null) {
                entity.setId(vo.getId());
            }
        }
        return entity;
    }
}
