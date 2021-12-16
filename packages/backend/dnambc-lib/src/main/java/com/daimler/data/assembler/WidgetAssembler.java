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

import com.daimler.data.db.entities.WidgetNsql;
import com.daimler.data.db.jsonb.Widget;
import com.daimler.data.db.jsonb.WidgetEntry;
import com.daimler.data.db.jsonb.WidgetUserRole;
import com.daimler.data.dto.widget.UserRoleVO;
import com.daimler.data.dto.widget.WidgetEntryVO;
import com.daimler.data.dto.widget.WidgetVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class WidgetAssembler implements GenericAssembler<WidgetVO, WidgetNsql> {

    @Override
    public WidgetVO toVo(WidgetNsql entity) {
    	WidgetVO widgetVO = null;
        if (Objects.nonNull(entity)) {
        	widgetVO = new WidgetVO();
        	Widget widget = entity.getData();
        	widgetVO.setId(entity.getId());
        	if(widget!=null) {
        		BeanUtils.copyProperties(widget, widgetVO);
        		if(widget.getWidgetChartType()!=null){
        			widgetVO.setWidgetChartType(WidgetVO.WidgetChartTypeEnum.valueOf(widget.getWidgetChartType()));
				}
        		List<WidgetEntry> dataEntries = widget.getDataEntries();
        		if (dataEntries != null && !dataEntries.isEmpty()) {
        			List<WidgetEntryVO> widgetEntriesVO = new ArrayList<>();
        			widgetEntriesVO = dataEntries.stream().map(n -> this.toWidgetEntryVO(n)).collect(Collectors.toList());
        			widgetVO.setDataEntries(widgetEntriesVO);
        		}
                    
        		List<WidgetUserRole> accessRoles = widget.getAccessRoles();
        		if (accessRoles != null && !accessRoles.isEmpty()) {
        			List<UserRoleVO> widgetUserRolesVO = new ArrayList<>();
        			widgetUserRolesVO = accessRoles.stream().map(n -> this.toWidgetUserRoleVO(n)).collect(Collectors.toList());
        			widgetVO.setAccessRoles(widgetUserRolesVO);
        		}
        	}
        }
        return widgetVO;
    }

    private WidgetEntryVO toWidgetEntryVO(WidgetEntry widgetEntry) {
    	WidgetEntryVO vo = new WidgetEntryVO();
    	if(widgetEntry != null) {
    		BeanUtils.copyProperties(widgetEntry, vo);
    	}
    	return vo;
    }
    
    private UserRoleVO toWidgetUserRoleVO(WidgetUserRole widgetUserRole) {
    	UserRoleVO vo = new UserRoleVO();
    	if(widgetUserRole != null) {
    		BeanUtils.copyProperties(widgetUserRole, vo);
    	}
    	return vo;
    }
    
    @Override
    public WidgetNsql toEntity(WidgetVO vo) {
    	WidgetNsql widgetNsql = null;
        if (Objects.nonNull(vo)) {
        	widgetNsql = new WidgetNsql();
            Widget widget = new Widget();
            BeanUtils.copyProperties(vo,widget);
            List<UserRoleVO> userRolesVO = vo.getAccessRoles();
            if(userRolesVO !=null && !userRolesVO.isEmpty()) {
            	List<WidgetUserRole> widgetUserRoles = new ArrayList<>();
            	widgetUserRoles = userRolesVO.stream().map(n -> this.toWidgetUserRole(n)).collect(Collectors.toList());
            	widget.setAccessRoles(widgetUserRoles);
            }
            List<WidgetEntryVO> widgetEntriesVO = vo.getDataEntries();
            if(widgetEntriesVO !=null && !widgetEntriesVO.isEmpty()) {
            	List<WidgetEntry> widgetEntries = new ArrayList<>();
            	widgetEntries = widgetEntriesVO.stream().map(n -> this.toWidgetEntry(n)).collect(Collectors.toList());
            	widget.setDataEntries(widgetEntries);
            }
            if(vo.getWidgetChartType()!=null){
            	widget.setWidgetChartType(vo.getWidgetChartType().name());
			}
            widget.setName(vo.getName() );
            widgetNsql.setData(widget);
            if (vo.getId() != null)
            	widgetNsql.setId(vo.getId());
        }
        return widgetNsql;
    }
    
    private WidgetUserRole toWidgetUserRole(UserRoleVO widgetUserRoleVO ) {
    	WidgetUserRole widgetUserRole = new WidgetUserRole();
    	if(widgetUserRoleVO != null) {
    		BeanUtils.copyProperties(widgetUserRoleVO, widgetUserRole);
    	}
    	return widgetUserRole;
    }

    private WidgetEntry toWidgetEntry(WidgetEntryVO widgetEntryVO) {
    	WidgetEntry widgetEntry = new WidgetEntry();
    	if(widgetEntryVO != null) {
    		BeanUtils.copyProperties(widgetEntryVO, widgetEntry);
    	}
    	return widgetEntry;
    }
}
