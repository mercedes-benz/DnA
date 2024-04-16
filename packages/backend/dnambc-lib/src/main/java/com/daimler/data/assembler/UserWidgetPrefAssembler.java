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

import com.daimler.data.db.entities.UserWidgetPreferenceNsql;
import com.daimler.data.db.jsonb.*;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.db.jsonb.solution.SolutionLocation;
import com.daimler.data.db.jsonb.solution.SolutionPhase;
import com.daimler.data.db.jsonb.solution.SolutionProjectStatus;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.dto.solution.SolutionLocationVO;
import com.daimler.data.dto.solution.SolutionPhaseVO;
import com.daimler.data.dto.solution.SolutionProjectStatusVO;
import com.daimler.data.dto.userwidgetpref.*;
import com.daimler.data.dto.userwidgetpref.FilterPreferencesVO.UseCaseTypeEnum;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserWidgetPrefAssembler implements GenericAssembler<UserWidgetPreferenceVO, UserWidgetPreferenceNsql> {

	private WidgetPreferenceVO toWidgetPreferenceVO(WidgetPreference widgetPref) {
		WidgetPreferenceVO vo = null;
		if (widgetPref != null) {
			vo = new WidgetPreferenceVO();
			vo.setId(widgetPref.getId());
			vo.setColumnPosition(widgetPref.getColumnPosition());
			vo.setRowPosition(widgetPref.getRowPosition());
			WidgetInfo widget = widgetPref.getWidget();
			WidgetVO widgetVO = new WidgetVO();
			if (widget != null) {
				BeanUtils.copyProperties(widget, widgetVO);
			}
			vo.setWidget(widgetVO);
		}
		return vo;
	}

	private SolutionLocationVO toSolutionLocationVO(SolutionLocation location) {
		SolutionLocationVO locationVO = null;
		if (location != null) {
			locationVO = new SolutionLocationVO();
			BeanUtils.copyProperties(location, locationVO);
		}
		return locationVO;
	}

	private DivisionVO toSolutionDivisionVO(UserWidgetPreferenceDivision entity) {
		DivisionVO vo = null;
		if (entity != null) {
			vo = new DivisionVO();
			BeanUtils.copyProperties(entity, vo);
			List<SubdivisionVO> subDivisionsVO = new ArrayList<SubdivisionVO>();
			List<SubDivision> subdivisions = entity.getSubdivisions();
			if (subdivisions != null && !subdivisions.isEmpty()) {
				for (SubDivision subDivision : subdivisions) {
					SubdivisionVO subdivisionVO = new SubdivisionVO();
					BeanUtils.copyProperties(subDivision, subdivisionVO);
					subDivisionsVO.add(subdivisionVO);
				}
			}

			vo.setSubdivisions(subDivisionsVO);
			;
		}
		return vo;
	}

	private SolutionPhaseVO toSolutionPhaseVO(SolutionPhase entity) {
		SolutionPhaseVO vo = null;
		if (entity != null) {
			vo = new SolutionPhaseVO();
			BeanUtils.copyProperties(entity, vo);
		}
		return vo;
	}

	private TagVO toTagVO(Tag entity) {
		TagVO vo = null;
		if (entity != null) {
			vo = new TagVO();
			BeanUtils.copyProperties(entity, vo);
		}
		return vo;
	}

	private SolutionProjectStatusVO toSolutionProjectstatusVO(SolutionProjectStatus entity) {
		SolutionProjectStatusVO vo = null;
		if (entity != null) {
			vo = new SolutionProjectStatusVO();
			BeanUtils.copyProperties(entity, vo);
		}
		return vo;
	}

	@Override
	public UserWidgetPreferenceVO toVo(UserWidgetPreferenceNsql entity) {
		UserWidgetPreferenceVO userWidgetPreferenceVO = null;
		if (Objects.nonNull(entity)) {
			userWidgetPreferenceVO = new UserWidgetPreferenceVO();
			UserWidgetPreference userWidgetPreference = entity.getData();
			userWidgetPreferenceVO.setId(entity.getId());
			if (userWidgetPreference != null) {

//        		UserInfoVO user = new UserInfoVO();
//        		user.setId(userWidgetPreference.getUserId());
//        		user.setId(userWidgetPreference.getUserId());
				userWidgetPreferenceVO.setUserId(userWidgetPreference.getUserId());

				List<WidgetPreference> widgetPrefs = userWidgetPreference.getWidgetPreferences();
				List<WidgetPreferenceVO> widgetPrefVOs = new ArrayList<>();
				if (widgetPrefs != null && !widgetPrefs.isEmpty()) {
					widgetPrefVOs = widgetPrefs.stream().map(n -> this.toWidgetPreferenceVO(n))
							.collect(Collectors.toList());
				}
				userWidgetPreferenceVO.setWidgetPreferences(widgetPrefVOs);

				FilterPreferences filterPrefs = userWidgetPreference.getFilterPreferences();
				FilterPreferencesVO filterPrefVO = new FilterPreferencesVO();
				if (filterPrefs != null) {

					filterPrefVO.useCaseType(UseCaseTypeEnum.fromValue(filterPrefs.getUseCaseType()));
					filterPrefVO.setDataValueRange(filterPrefs.getDataValueRange());
					List<UserWidgetPreferenceDivision> divisions = filterPrefs.getDivisions();
					List<DivisionVO> divisionsVO = new ArrayList<>();
					if (divisions != null && !divisions.isEmpty()) {
						divisionsVO = divisions.stream().map(n -> this.toSolutionDivisionVO(n))
								.collect(Collectors.toList());
					}
					filterPrefVO.setDivisions(divisionsVO);

					List<SolutionLocation> locations = filterPrefs.getLocations();
					List<SolutionLocationVO> locationsVO = new ArrayList<>();
					if (locations != null && !locations.isEmpty()) {
						locationsVO = locations.stream().map(n -> this.toSolutionLocationVO(n))
								.collect(Collectors.toList());
					}
					filterPrefVO.setLocations(locationsVO);

					List<SolutionPhase> phases = filterPrefs.getPhases();
					List<SolutionPhaseVO> phasesVO = new ArrayList<>();
					if (phases != null && !phases.isEmpty()) {
						phasesVO = phases.stream().map(n -> this.toSolutionPhaseVO(n)).collect(Collectors.toList());
					}
					filterPrefVO.setPhases(phasesVO);

					SolutionProjectStatus status = filterPrefs.getSolutionStatus();
					SolutionProjectStatusVO statusVO = new SolutionProjectStatusVO();
					if (status != null) {
						statusVO = this.toSolutionProjectstatusVO(status);
					}
					filterPrefVO.setSolutionStatus(statusVO);
					List<Tag> tags = filterPrefs.getTags();
					List<TagVO> tagVos = new ArrayList<>();
					if (tags != null && !tags.isEmpty()) {
						tagVos = tags.stream().map(n -> this.toTagVO(n)).collect(Collectors.toList());
					}
					filterPrefVO.setTags(tagVos);

				}
				userWidgetPreferenceVO.setFilterPreferences(filterPrefVO);
			}
		}
		return userWidgetPreferenceVO;
	}

	private WidgetPreference toWidgetPreference(WidgetPreferenceVO widgetPrefVO) {
		WidgetPreference entity = null;
		if (widgetPrefVO != null) {
			entity = new WidgetPreference();
			entity.setId(widgetPrefVO.getId());
			entity.setColumnPosition(widgetPrefVO.getColumnPosition());
			entity.setRowPosition(widgetPrefVO.getRowPosition());
			WidgetInfo widget = new WidgetInfo();
			WidgetVO widgetVO = widgetPrefVO.getWidget();
			if (widgetVO != null) {
				BeanUtils.copyProperties(widgetVO, widget);
			}
			entity.setWidget(widget);
		}
		return entity;
	}

	private SolutionLocation toSolutionLocation(SolutionLocationVO vo) {
		SolutionLocation entity = null;
		if (vo != null) {
			entity = new SolutionLocation();
			BeanUtils.copyProperties(vo, entity);
		}
		return entity;
	}

	private UserWidgetPreferenceDivision toSolutionDivision(DivisionVO vo) {
		UserWidgetPreferenceDivision entity = null;
		if (vo != null) {
			entity = new UserWidgetPreferenceDivision();
			BeanUtils.copyProperties(vo, entity);
			SubDivision subdivision = null;
			List<SubDivision> subdivisions = new ArrayList<SubDivision>();
			List<SubdivisionVO> subDivisionsVO = vo.getSubdivisions();
			if (subDivisionsVO != null) {
				for (SubdivisionVO subdivisionVO : subDivisionsVO) {
					subdivision = new SubDivision();
					BeanUtils.copyProperties(subdivisionVO, subdivision);
					subdivisions.add(subdivision);
				}
			}
			entity.setSubdivisions(subdivisions);
		}
		return entity;
	}

	private SolutionPhase toSolutionPhase(SolutionPhaseVO vo) {
		SolutionPhase entity = null;
		if (vo != null) {
			entity = new SolutionPhase();
			BeanUtils.copyProperties(vo, entity);
		}
		return entity;
	}

	private Tag toTag(TagVO vo) {
		Tag entity = null;
		if (vo != null) {
			entity = new Tag();
			BeanUtils.copyProperties(vo, entity);
		}
		return entity;
	}

	private SolutionProjectStatus toSolutionProjectstatus(SolutionProjectStatusVO vo) {
		SolutionProjectStatus entity = null;
		if (vo != null) {
			entity = new SolutionProjectStatus();
			BeanUtils.copyProperties(vo, entity);
		}
		return entity;
	}

	@Override
	public UserWidgetPreferenceNsql toEntity(UserWidgetPreferenceVO vo) {
		UserWidgetPreferenceNsql userWidgetPreferenceNsql = null;
		if (vo != null) {
			userWidgetPreferenceNsql = new UserWidgetPreferenceNsql();
			UserWidgetPreference data = new UserWidgetPreference();
			if (vo.getId() != null)
				userWidgetPreferenceNsql.setId(vo.getId());
			data.setUserId(vo.getUserId());
//    		UserInfoVO userInfo = vo.getUser();
//    		if(userInfo != null)
//    			data.setUserId(userInfo.getId());UserInfoVO userInfo = vo.getUser();
//    		if(userInfo != null)
//    			data.setUserId(userInfo.getId());

			List<WidgetPreferenceVO> widgetPrefVOs = vo.getWidgetPreferences();
			List<WidgetPreference> widgetPrefs = new ArrayList<>();
			if (widgetPrefVOs != null && !widgetPrefVOs.isEmpty()) {
				widgetPrefs = widgetPrefVOs.stream().map(n -> this.toWidgetPreference(n)).collect(Collectors.toList());
			}
			data.setWidgetPreferences(widgetPrefs);

			FilterPreferencesVO filterPrefVO = vo.getFilterPreferences();
			FilterPreferences filterPref = new FilterPreferences();
			if (filterPrefVO != null) {
				if (filterPrefVO.getUseCaseType() != null)
					filterPref.setUseCaseType(filterPrefVO.getUseCaseType().toString());
				filterPref.setDataValueRange(filterPrefVO.getDataValueRange());
				List<DivisionVO> divisionsVO = filterPrefVO.getDivisions();
				List<UserWidgetPreferenceDivision> divisions = new ArrayList<>();
				if (divisionsVO != null && !divisionsVO.isEmpty()) {
					divisions = divisionsVO.stream().map(n -> this.toSolutionDivision(n)).collect(Collectors.toList());
				}
				filterPref.setDivisions(divisions);

				List<SolutionLocationVO> locationsVO = filterPrefVO.getLocations();
				List<SolutionLocation> locations = new ArrayList<>();
				if (locationsVO != null && !locationsVO.isEmpty()) {
					locations = locationsVO.stream().map(n -> this.toSolutionLocation(n)).collect(Collectors.toList());
				}
				filterPref.setLocations(locations);

				List<SolutionPhaseVO> phasesVO = filterPrefVO.getPhases();
				List<SolutionPhase> phases = new ArrayList<>();
				if (phasesVO != null && !phasesVO.isEmpty()) {
					phases = phasesVO.stream().map(n -> this.toSolutionPhase(n)).collect(Collectors.toList());
				}
				filterPref.setPhases(phases);

				SolutionProjectStatusVO statusVO = filterPrefVO.getSolutionStatus();
				SolutionProjectStatus status = new SolutionProjectStatus();
				if (statusVO != null) {
					status = this.toSolutionProjectstatus(statusVO);
				}
				filterPref.setSolutionStatus(status);
				List<Tag> tags = new ArrayList<>();
				List<TagVO> tagVos = filterPrefVO.getTags();
				if (tagVos != null && !tagVos.isEmpty()) {
					tags = tagVos.stream().map(n -> this.toTag(n)).collect(Collectors.toList());
				}
				filterPref.setTags(tags);
			}
			data.setFilterPreferences(filterPref);
			userWidgetPreferenceNsql.setData(data);
		}
		return userWidgetPreferenceNsql;
	}

}
