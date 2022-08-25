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

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.CodeServerWorkspaceNsql;
import com.daimler.data.db.json.CodeServerWorkspace;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.CloudServiceProviderEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.CpuCapacityEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.EnvironmentEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.OperatingSystemEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.RamMetricsEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.RamSizeEnum;
import com.daimler.data.dto.workspace.CodeServerWorkspaceVO.RecipeIdEnum;

@Component
public class WorkspaceAssembler implements GenericAssembler<CodeServerWorkspaceVO, CodeServerWorkspaceNsql> {

	@Override
	public CodeServerWorkspaceVO toVo(CodeServerWorkspaceNsql entity) {
		CodeServerWorkspaceVO vo = new CodeServerWorkspaceVO();
		if(entity!=null) {
			vo.setId(entity.getId());
			CodeServerWorkspace data = entity.getData();
			if(data!=null) {
				BeanUtils.copyProperties(data, vo);
				vo.setCloudServiceProvider(CloudServiceProviderEnum.fromValue(data.getCloudServiceProvider()));
				vo.setRecipeId(RecipeIdEnum.fromValue(data.getRecipeId()));
				vo.setRamSize(RamSizeEnum.fromValue(data.getRamSize().split(" ")[0]));
				vo.setCpuCapacity(CpuCapacityEnum.fromValue(data.getCpuCapacity()));
				vo.setEnvironment(EnvironmentEnum.fromValue(data.getEnvironment()));
				vo.setOperatingSystem(OperatingSystemEnum.fromValue(data.getOperatingSystem()));
				vo.setRamMetrics(RamMetricsEnum.GB);
				vo.setCloudServiceProvider(CloudServiceProviderEnum.fromValue(data.getCloudServiceProvider()));
				vo.setCloudServiceProvider(CloudServiceProviderEnum.fromValue(data.getCloudServiceProvider()));
			}
		}
		return vo;
	}

	@Override
	public CodeServerWorkspaceNsql toEntity(CodeServerWorkspaceVO vo) {
		CodeServerWorkspaceNsql entity = null;
		if(vo!=null) {
			entity = new CodeServerWorkspaceNsql();
			CodeServerWorkspace data = new CodeServerWorkspace();
			BeanUtils.copyProperties(vo,data);
			data.setRecipeId(vo.getRecipeId().toString());
			data.setRamSize(vo.getRamSize().toString() + " " + vo.getRamMetrics().toString());
			data.setCpuCapacity(vo.getCpuCapacity().toString());
			data.setOperatingSystem(vo.getOperatingSystem().toString());
			data.setEnvironment(vo.getEnvironment().toString());
			data.setCloudServiceProvider(vo.getCloudServiceProvider().toString());
			entity.setData(data);
			if (vo.getId() != null)
				entity.setId(vo.getId());
		}
		return entity;
	}

	
}
