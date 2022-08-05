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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DivisionNsql;
import com.daimler.data.db.jsonb.Division;
import com.daimler.data.db.jsonb.SubDivision;
import com.daimler.data.db.jsonb.solution.ChangeLogs;
import com.daimler.data.db.jsonb.solution.SolutionTeamMember;
import com.daimler.data.dto.divisions.DivisionVO;
import com.daimler.data.dto.divisions.SubdivisionVO;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.TeamMemberVO;
import com.daimler.data.dto.solution.TeamMemberVO.UserTypeEnum;

@Component
public class DivisionAssembler implements GenericAssembler<DivisionVO, DivisionNsql> {

	@Override
	public DivisionVO toVo(DivisionNsql entity) {
		DivisionVO divisionVO = null;
		if (Objects.nonNull(entity)) {
			divisionVO = new DivisionVO();
			divisionVO.setId(entity.getId());
			divisionVO.setName(entity.getData().getName());
			List<SubdivisionVO> subdivisionVOs = new ArrayList<SubdivisionVO>();
			if (null != entity.getData().getSubdivisions() && !entity.getData().getSubdivisions().isEmpty()) {
				for (SubDivision subDivision : entity.getData().getSubdivisions()) {
					SubdivisionVO vo = new SubdivisionVO();
					BeanUtils.copyProperties(subDivision, vo);
					subdivisionVOs.add(vo);
				}
			}
			divisionVO.setSubdivisions(subdivisionVOs);
			List<ChangeLogs> changeLogsList = entity.getData().getChangeLogs();
			if (changeLogsList != null && !changeLogsList.isEmpty()) {
				List<ChangeLogVO> changeLogVOList = new ArrayList<>();
				for (ChangeLogs changeLogs : changeLogsList) {
					ChangeLogVO changeLogVO = new ChangeLogVO();
					BeanUtils.copyProperties(changeLogs, changeLogVO);
					if (null != changeLogs.getModifiedBy()) {
						TeamMemberVO teamMemberVO = new TeamMemberVO();
						BeanUtils.copyProperties(changeLogs.getModifiedBy(), teamMemberVO);
						if (StringUtils.hasText(changeLogs.getModifiedBy().getUserType())) {
							teamMemberVO
									.setUserType(UserTypeEnum.valueOf(changeLogs.getModifiedBy().getUserType()));
						}
						changeLogVO.setModifiedBy(teamMemberVO);
					}
					changeLogVOList.add(changeLogVO);
				}
				divisionVO.setChangeLogs(changeLogVOList);
			}
		}
		return divisionVO;
	}

	@Override
	public DivisionNsql toEntity(DivisionVO vo) {
		DivisionNsql divisionNsql = null;
		if (Objects.nonNull(vo)) {
			divisionNsql = new DivisionNsql();
			Division division = new Division();
			division.setName(vo.getName().toUpperCase());
			if (!ObjectUtils.isEmpty(vo.getSubdivisions())) {
				List<SubDivision> subdivisions = new ArrayList<SubDivision>();
				subdivisions = vo.getSubdivisions().stream().map(n -> toSubDivision(n)).collect(Collectors.toList());
				List<SubDivision> uniqueSubDivision = subdivisions.stream().collect(Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(subdivisionComp)), ArrayList::new));
				division.setSubdivisions(uniqueSubDivision);
			}
			List<ChangeLogVO> changeLogVOList = vo.getChangeLogs();
			List<ChangeLogs> changeLogsList = new ArrayList<>();
			if (null != changeLogVOList && !changeLogVOList.isEmpty()) {
				changeLogsList = changeLogVOList.stream().map(n -> toChangeLogsJson(n))
						.collect(Collectors.toList());
				division.setChangeLogs(changeLogsList);
			}
			
			divisionNsql.setData(division);
			if (StringUtils.hasText(vo.getId())) {
				divisionNsql.setId(vo.getId());
			}

		}
		return divisionNsql;
	}

	private ChangeLogs toChangeLogsJson(ChangeLogVO changeLogVO) {
		ChangeLogs changeLogs = new ChangeLogs();
		if (null != changeLogVO) {
			BeanUtils.copyProperties(changeLogVO, changeLogs);
			if (null != changeLogVO.getModifiedBy()) {
				SolutionTeamMember solutionTeamMember = new SolutionTeamMember();
				BeanUtils.copyProperties(changeLogVO.getModifiedBy(), solutionTeamMember);
				if (!StringUtils.isEmpty(changeLogVO.getModifiedBy().getUserType()))
					solutionTeamMember.setUserType(changeLogVO.getModifiedBy().getUserType().name());
				changeLogs.setModifiedBy(solutionTeamMember);
			}
		}
		return changeLogs;
	}
	
	private Comparator<SubDivision> subdivisionComp = new Comparator<SubDivision>() {
		@Override
		public int compare(SubDivision s1, SubDivision s2) {
			if (s2.getName().equals(s1.getName())) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	public List<SubdivisionVO> toSubDivisionVoList(DivisionNsql entity) {
		List<SubdivisionVO> subdivisionsVo = new ArrayList<>();
		if (Objects.nonNull(entity)) {
			Division division = entity.getData();
			if (division != null) {
				List<SubDivision> subDivisions = division.getSubdivisions();
				if (subDivisions != null && !subDivisions.isEmpty())
					subdivisionsVo = subDivisions.stream().map(n -> this.toSubDivisionVo(n))
							.collect(Collectors.toList());
			}
		}
		return subdivisionsVo;

	}

	public SubdivisionVO toSubDivisionVo(SubDivision subdivision) {
		SubdivisionVO vo = null;
		if (subdivision != null) {
			vo = new SubdivisionVO();
			vo.setId(subdivision.getId());
			vo.setName(subdivision.getName());
		}
		return vo;
	}

	public SubDivision toSubDivision(SubdivisionVO subdivisionVo) {
		SubDivision subdivision = null;
		if (subdivisionVo != null) {
			subdivision = new SubDivision();
			if (StringUtils.hasText(subdivisionVo.getId())) {
				subdivision.setId(subdivisionVo.getId());
			} else {
				subdivision.setId(UUID.randomUUID().toString());
			}
			subdivision.setName(subdivisionVo.getName().toUpperCase());
		}
		return subdivision;
	}

}
