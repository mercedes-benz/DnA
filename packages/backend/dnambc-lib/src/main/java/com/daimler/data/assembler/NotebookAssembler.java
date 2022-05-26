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

import com.daimler.data.db.entities.NotebookNsql;
import com.daimler.data.db.jsonb.Notebook;
import com.daimler.data.dto.notebook.NotebookVO;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
public class NotebookAssembler implements GenericAssembler<NotebookVO, NotebookNsql> {

	@Override
	public NotebookVO toVo(NotebookNsql entity) {
		NotebookVO notebookVO = null;
		if (Objects.nonNull(entity)) {
			notebookVO = new NotebookVO();
			notebookVO.setId(entity.getId());
			Notebook notebook = entity.getData();
			if (notebook != null) {
				notebookVO.setName(notebook.getName());
				notebookVO.setDescription(notebook.getDescription());
				notebookVO.setCreatedOn(notebook.getCreatedOn());
				notebookVO.setUserId(notebook.getUserId());
				notebookVO.setSolutionId(notebook.getSolutionId());

			}
		}
		return notebookVO;
	}

	@Override
	public NotebookNsql toEntity(NotebookVO vo) {
		NotebookNsql notebookNsql = null;
		if (Objects.nonNull(vo)) {
			notebookNsql = new NotebookNsql();
			Notebook notebook = new Notebook();
			notebook.setName(vo.getName());
			notebook.setSolutionId(vo.getSolutionId());
			notebook.setDescription(vo.getDescription());
			notebook.setCreatedOn(vo.getCreatedOn());
			notebook.setUserId(vo.getUserId());
			notebookNsql.setData(notebook);
			if (vo.getId() != null)
				notebookNsql.setId(vo.getId());
		}
		return notebookNsql;
	}
	
	/**
	 * To convert UserInfoVO to CreatedByVO
	 * 
	 * @param userInfoVO
	 * @return CreatedByVO
	 */
	public CreatedByVO toCreatedByVO(UserInfoVO userInfoVO) {
		CreatedByVO createdByVO = null;
		if(Objects.nonNull(userInfoVO)) {
			createdByVO = new CreatedByVO();
			BeanUtils.copyProperties(userInfoVO, createdByVO);
		}
		return createdByVO;
	}
	
}
