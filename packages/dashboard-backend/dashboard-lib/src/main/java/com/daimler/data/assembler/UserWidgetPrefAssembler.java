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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.daimler.data.db.entities.UserWidgetPreferenceNsql;
import com.daimler.data.db.jsonb.report.Subdivision;
import com.daimler.data.db.jsonb.userwidgetpref.Art;
import com.daimler.data.db.jsonb.userwidgetpref.FilterPreferences;
import com.daimler.data.db.jsonb.userwidgetpref.UserWidgetPreference;
import com.daimler.data.db.jsonb.userwidgetpref.UserWidgetPreferenceDivision;
import com.daimler.data.dto.report.ArtVO;
import com.daimler.data.dto.report.SubdivisionVO;
import com.daimler.data.dto.userwidgetpref.FilterPreferencesVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPrefDivisionVO;
import com.daimler.data.dto.userwidgetpref.UserWidgetPreferenceVO;

@Component
public class UserWidgetPrefAssembler implements GenericAssembler<UserWidgetPreferenceVO, UserWidgetPreferenceNsql> {

	private ArtVO toArtVO(Art art) {
		ArtVO artVO = null;
		if (art != null) {
			artVO = new ArtVO();
			BeanUtils.copyProperties(art, artVO);
		}
		return artVO;
	}

	private UserWidgetPrefDivisionVO toDivisionVO(UserWidgetPreferenceDivision entity) {
		UserWidgetPrefDivisionVO vo = null;
		if (entity != null) {
			vo = new UserWidgetPrefDivisionVO();
			BeanUtils.copyProperties(entity, vo);
			List<SubdivisionVO> subDivisionsVO = new ArrayList<SubdivisionVO>();
			List<Subdivision> subdivisions = entity.getSubdivisions();
			if (subdivisions != null && !subdivisions.isEmpty()) {
				for (Subdivision subDivision : subdivisions) {
					SubdivisionVO subdivisionVO = new SubdivisionVO();
					BeanUtils.copyProperties(subDivision, subdivisionVO);
					subDivisionsVO.add(subdivisionVO);
				}
			}

			vo.setSubdivisions(subDivisionsVO);
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
				userWidgetPreferenceVO.setUserId(userWidgetPreference.getUserId());

				FilterPreferences filterPrefs = userWidgetPreference.getFilterPreferences();
				FilterPreferencesVO filterPrefVO = new FilterPreferencesVO();
				if (filterPrefs != null) {
					BeanUtils.copyProperties(filterPrefs, filterPrefVO);
					List<UserWidgetPreferenceDivision> divisions = filterPrefs.getDivisions();
					List<UserWidgetPrefDivisionVO> divisionsVO = new ArrayList<>();
					if (divisions != null && !divisions.isEmpty()) {
						divisionsVO = divisions.stream().map(n -> this.toDivisionVO(n)).collect(Collectors.toList());
					}
					filterPrefVO.setDivisions(divisionsVO);

					List<Art> arts = filterPrefs.getArts();
					List<ArtVO> artsVO = new ArrayList<>();
					if (arts != null && !arts.isEmpty()) {
						artsVO = arts.stream().map(n -> this.toArtVO(n)).collect(Collectors.toList());
					}
					filterPrefVO.setArts(artsVO);

				}
				userWidgetPreferenceVO.setFilterPreferences(filterPrefVO);
			}
		}
		return userWidgetPreferenceVO;
	}

	private Art toArt(ArtVO vo) {
		Art entity = null;
		if (vo != null) {
			entity = new Art();
			BeanUtils.copyProperties(vo, entity);
		}
		return entity;
	}

	private UserWidgetPreferenceDivision toDivision(UserWidgetPrefDivisionVO vo) {
		UserWidgetPreferenceDivision entity = null;
		if (vo != null) {
			entity = new UserWidgetPreferenceDivision();
			BeanUtils.copyProperties(vo, entity);
			Subdivision subdivision = null;
			List<Subdivision> subdivisions = new ArrayList<Subdivision>();
			List<SubdivisionVO> subDivisionsVO = vo.getSubdivisions();
			if (subDivisionsVO != null) {
				for (SubdivisionVO subdivisionVO : subDivisionsVO) {
					subdivision = new Subdivision();
					BeanUtils.copyProperties(subdivisionVO, subdivision);
					subdivisions.add(subdivision);
				}
			}
			entity.setSubdivisions(subdivisions);
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

			FilterPreferencesVO filterPrefVO = vo.getFilterPreferences();
			FilterPreferences filterPref = new FilterPreferences();
			if (filterPrefVO != null) {
				BeanUtils.copyProperties(filterPrefVO, filterPref);
				List<UserWidgetPrefDivisionVO> divisionsVO = filterPrefVO.getDivisions();
				List<UserWidgetPreferenceDivision> divisions = new ArrayList<>();
				if (divisionsVO != null && !divisionsVO.isEmpty()) {
					divisions = divisionsVO.stream().map(n -> this.toDivision(n)).collect(Collectors.toList());
				}
				filterPref.setDivisions(divisions);

				List<ArtVO> artsVO = filterPrefVO.getArts();
				List<Art> arts = new ArrayList<>();
				if (artsVO != null && !artsVO.isEmpty()) {
					arts = artsVO.stream().map(n -> this.toArt(n)).collect(Collectors.toList());
				}
				filterPref.setArts(arts);
			}
			data.setFilterPreferences(filterPref);
			userWidgetPreferenceNsql.setData(data);
		}
		return userWidgetPreferenceNsql;
	}

}
