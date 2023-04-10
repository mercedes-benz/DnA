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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.jsonb.report.CreatedBy;
import com.daimler.data.db.jsonb.report.Customer;
import com.daimler.data.db.jsonb.report.DataSource;
import com.daimler.data.db.jsonb.report.DataWarehouse;
import com.daimler.data.db.jsonb.report.Description;
import com.daimler.data.db.jsonb.report.Division;
import com.daimler.data.db.jsonb.report.ExternalCustomer;
import com.daimler.data.db.jsonb.report.InternalCustomer;
import com.daimler.data.db.jsonb.report.KPI;
import com.daimler.data.db.jsonb.report.Member;
import com.daimler.data.db.jsonb.report.Report;
import com.daimler.data.db.jsonb.report.SingleDataSource;
import com.daimler.data.db.jsonb.report.Subdivision;
import com.daimler.data.db.jsonb.report.TeamMember;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.report.CustomerVO;
import com.daimler.data.dto.report.DataAndFunctionVO;
import com.daimler.data.dto.report.DataSourceVO;
import com.daimler.data.dto.report.DataWarehouseVO;
import com.daimler.data.dto.report.DescriptionVO;
import com.daimler.data.dto.report.DivisionVO;
import com.daimler.data.dto.report.ExternalCustomerVO;
import com.daimler.data.dto.report.InternalCustomerVO;
import com.daimler.data.dto.report.KPIVO;
import com.daimler.data.dto.report.MemberVO;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.dto.report.SingleDataSourceVO;
import com.daimler.data.dto.report.SubdivisionVO;
import com.daimler.data.dto.report.TeamMemberVO;
import com.daimler.data.dto.report.TeamMemberVO.UserTypeEnum;

@Component
public class ReportAssembler implements GenericAssembler<ReportVO, ReportNsql> {

	@Override
	public ReportVO toVo(ReportNsql entity) {
		ReportVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new ReportVO();
			Report report = entity.getData();
			BeanUtils.copyProperties(report, vo);
			if (report.getCreatedBy() != null) {
				vo.setCreatedBy(new CreatedByVO().id(report.getCreatedBy().getId())
						.firstName(report.getCreatedBy().getFirstName()).lastName(report.getCreatedBy().getLastName())
						.department(report.getCreatedBy().getDepartment()).email(report.getCreatedBy().getEmail())
						.department(report.getCreatedBy().getDepartment()));
			}
			if (report.getDescription() != null) {
				DescriptionVO descriptionVO = new DescriptionVO();
				BeanUtils.copyProperties(report.getDescription(), descriptionVO);
				descriptionVO.setDivision(toDivisionVO(report.getDescription().getDivision()));
				vo.setDescription(descriptionVO);
			}
			if (report.getCustomer() != null) {
				CustomerVO customerVO = new CustomerVO();
				BeanUtils.copyProperties(report.getCustomer(), customerVO);
				if (!ObjectUtils.isEmpty(report.getCustomer().getInternalCustomers())) {
					List<InternalCustomerVO> internalCustomers = report.getCustomer().getInternalCustomers().stream()
							.map(n -> toInternalCustomerVO(n)).collect(Collectors.toList());
					customerVO.setInternalCustomers(internalCustomers);
				}
				if (!ObjectUtils.isEmpty(report.getCustomer().getExternalCustomers())) {
					List<ExternalCustomerVO> externalCustomers = report.getCustomer().getExternalCustomers().stream()
							.map(n -> toExternalCustomerVO(n)).collect(Collectors.toList());
					customerVO.setExternalCustomers(externalCustomers);
				}

				vo.setCustomer(customerVO);
			}
			if (!ObjectUtils.isEmpty(report.getKpis())) {
				List<KPIVO> kpis = report.getKpis().stream().map(n -> toKPIVO(n)).collect(Collectors.toList());
				vo.setKpis(kpis);
			}
			DataAndFunctionVO dataAndFunctionVO = new DataAndFunctionVO();
			if (!ObjectUtils.isEmpty(report.getDataWarehouses())) {
				List<DataWarehouseVO> dataWarehouseVO = report.getDataWarehouses().stream()
						.map(n -> toDataWarehouseVO(n)).collect(Collectors.toList());
				dataAndFunctionVO.setDataWarehouseInUse(dataWarehouseVO);
			}

			if (!ObjectUtils.isEmpty(report.getSingleDataSources())) {
				List<SingleDataSourceVO> singleDataSourcesVO = report.getSingleDataSources().stream()
						.map(n -> toSingleDataSourceVO(n)).collect(Collectors.toList());
				dataAndFunctionVO.setSingleDataSources(singleDataSourcesVO);
			}
			vo.setDataAndFunctions(dataAndFunctionVO);
			if (report.getMember() != null) {
				MemberVO memberVO = new MemberVO();
				BeanUtils.copyProperties(report.getMember(), memberVO);
				if (!ObjectUtils.isEmpty(report.getMember().getReportOwners())) {
					List<TeamMemberVO> reportOwners = report.getMember().getReportOwners().stream()
							.map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
					memberVO.setReportOwners(reportOwners);
				}
				if (!ObjectUtils.isEmpty(report.getMember().getReportAdmins())) {
					List<TeamMemberVO> reportAdmins = report.getMember().getReportAdmins().stream()
							.map(n -> toTeamMemberVO(n)).collect(Collectors.toList());
					memberVO.setReportAdmins(reportAdmins);
				}
				vo.setMembers(memberVO);
			}
			if (!ObjectUtils.isEmpty(report.getOpenSegments())) {
				List<ReportVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
				report.getOpenSegments().forEach(
						openSegment -> openSegmentsEnumList.add(ReportVO.OpenSegmentsEnum.valueOf(openSegment)));
				vo.setOpenSegments(openSegmentsEnumList);
			}
			vo.setId(entity.getId());
		}

		return vo;
	}

	private DataWarehouseVO toDataWarehouseVO(DataWarehouse dataWarehouse) {
		DataWarehouseVO vo = null;
		if (dataWarehouse != null) {
			vo = new DataWarehouseVO();
			BeanUtils.copyProperties(dataWarehouse, vo);
		}
		return vo;
	}

	private SingleDataSourceVO toSingleDataSourceVO(SingleDataSource singleDataSource) {
		SingleDataSourceVO vo = null;
		if (singleDataSource != null) {
			vo = new SingleDataSourceVO();
			BeanUtils.copyProperties(singleDataSource, vo);
			List<DataSourceVO> dataSourcesVO = singleDataSource.getDataSources().stream().map(n -> toDataSourceVO(n))
					.collect(Collectors.toList());
			vo.setDataSources(dataSourcesVO);
		}
		return vo;
	}

	private DataSourceVO toDataSourceVO(DataSource dataSource) {
		DataSourceVO vo = null;
		if (dataSource != null) {
			vo = new DataSourceVO();
			BeanUtils.copyProperties(dataSource, vo);
		}
		return vo;
	}

	private KPIVO toKPIVO(KPI kpi) {
		KPIVO kPIVO = null;
		if (kpi != null) {
			kPIVO = new KPIVO();
			BeanUtils.copyProperties(kpi, kPIVO);
		}
		return kPIVO;
	}

	private InternalCustomerVO toInternalCustomerVO(InternalCustomer internalCustomer) {
		InternalCustomerVO vo = null;
		if (internalCustomer != null) {
			vo = new InternalCustomerVO();
			BeanUtils.copyProperties(internalCustomer, vo);
			vo.setDivision(toDivisionVO(internalCustomer.getDivision()));			
			vo.setProcessOwner(toTeamMemberVO(internalCustomer.getProcessOwner()));
		}
		return vo;
	}

	private ExternalCustomerVO toExternalCustomerVO(ExternalCustomer externalCustomer) {
		ExternalCustomerVO vo = null;
		if (externalCustomer != null) {
			vo = new ExternalCustomerVO();
			BeanUtils.copyProperties(externalCustomer, vo);
		}
		return vo;
	}

	private DivisionVO toDivisionVO(Division division) {
		DivisionVO vo = null;
		if (division != null) {
			vo = new DivisionVO();
			BeanUtils.copyProperties(division, vo);
			SubdivisionVO subdivisionVO = new SubdivisionVO();
			if (division.getSubdivision() != null) {
				BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
			}
			vo.setSubdivision(subdivisionVO);
		}
		return vo;
	}

	private TeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		TeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new TeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	@Override
	public ReportNsql toEntity(ReportVO vo) {
		ReportNsql entity = null;
		if (vo != null) {
			entity = new ReportNsql();
			String id = vo.getId();
			if (id != null && !id.isEmpty() && !id.trim().isEmpty()) {
				entity.setId(id);
			}
			Report report = new Report();
			BeanUtils.copyProperties(vo, report);
			report.setPublish(vo.isPublish());
			if (vo.getCreatedBy() != null) {
				report.setCreatedBy(new CreatedBy(vo.getCreatedBy().getId(), vo.getCreatedBy().getFirstName(),
						vo.getCreatedBy().getLastName(), vo.getCreatedBy().getDepartment(),
						vo.getCreatedBy().getEmail(), vo.getCreatedBy().getMobileNumber()));
			}

			if (vo.getDescription() != null) {
				Description description = new Description();
				BeanUtils.copyProperties(vo.getDescription(), description);
				description.setDivision(toDivisionJson(vo.getDescription().getDivision()));
				report.setDescription(description);
			}

			if (vo.getCustomer() != null) {
				Customer customer = new Customer();
				BeanUtils.copyProperties(vo.getCustomer(), customer);
				if (!ObjectUtils.isEmpty(vo.getCustomer().getInternalCustomers())) {
					List<InternalCustomer> internalCustomers = vo.getCustomer().getInternalCustomers().stream()
							.map(n -> toInternalCustomerJson(n)).collect(Collectors.toList());
					customer.setInternalCustomers(internalCustomers);
				}
				if (!ObjectUtils.isEmpty(vo.getCustomer().getExternalCustomers())) {
					List<ExternalCustomer> externalCustomers = vo.getCustomer().getExternalCustomers().stream()
							.map(n -> toExternalCustomerJson(n)).collect(Collectors.toList());
					customer.setExternalCustomers(externalCustomers);
				}
				report.setCustomer(customer);
			}
			if (!ObjectUtils.isEmpty(vo.getKpis())) {
				List<KPI> kpis = vo.getKpis().stream().map(n -> toKPIJson(n)).collect(Collectors.toList());
				report.setKpis(kpis);
			}
			if (Objects.nonNull(vo.getDataAndFunctions())
					&& !ObjectUtils.isEmpty(vo.getDataAndFunctions().getDataWarehouseInUse())) {
				List<DataWarehouse> dataWarehouseInUse = vo.getDataAndFunctions().getDataWarehouseInUse().stream()
						.map(n -> toDataWarehouseJson(n)).collect(Collectors.toList());
				report.setDataWarehouses(dataWarehouseInUse);
			}

			if (Objects.nonNull(vo.getDataAndFunctions())
					&& !ObjectUtils.isEmpty(vo.getDataAndFunctions().getSingleDataSources())) {
				List<SingleDataSource> singleDataSources = vo.getDataAndFunctions().getSingleDataSources().stream()
						.map(n -> toSingleDataSourceJson(n)).collect(Collectors.toList());
				report.setSingleDataSources(singleDataSources);
			}

			if (vo.getMembers() != null) {
				Member member = new Member();
				BeanUtils.copyProperties(vo.getMembers(), member);
				if (!ObjectUtils.isEmpty(vo.getMembers().getReportOwners())) {
					List<TeamMember> reportOwners = vo.getMembers().getReportOwners().stream()
							.map(n -> toTeamMemberJson(n)).collect(Collectors.toList());
					member.setReportOwners(reportOwners);
				}
				if (!ObjectUtils.isEmpty(vo.getMembers().getReportAdmins())) {
					List<TeamMember> reportAdmins = vo.getMembers().getReportAdmins().stream()
							.map(n -> toTeamMemberJson(n)).collect(Collectors.toList());
					member.setReportAdmins(reportAdmins);
				}
				report.setMember(member);
			}
			if (!ObjectUtils.isEmpty(vo.getOpenSegments())) {
				List<String> openSegmentList = new ArrayList<>();
				vo.getOpenSegments().forEach(openSegmentsEnum -> {
					openSegmentList.add(openSegmentsEnum.name());
				});
				report.setOpenSegments(openSegmentList);
			}
			entity.setData(report);
		}

		return entity;
	}

	private DataWarehouse toDataWarehouseJson(DataWarehouseVO vo) {
		DataWarehouse dataWarehouse = null;
		if (vo != null) {
			dataWarehouse = new DataWarehouse();
			BeanUtils.copyProperties(vo, dataWarehouse);
		}
		return dataWarehouse;
	}

	private SingleDataSource toSingleDataSourceJson(SingleDataSourceVO vo) {
		SingleDataSource singleDataSource = null;
		if (vo != null) {
			singleDataSource = new SingleDataSource();
			BeanUtils.copyProperties(vo, singleDataSource);
			List<DataSource> dataSources = vo.getDataSources().stream().map(n -> toDataSourceJson(n))
					.collect(Collectors.toList());
			singleDataSource.setDataSources(dataSources);
		}
		return singleDataSource;
	}

	private DataSource toDataSourceJson(DataSourceVO vo) {
		DataSource dataSource = null;
		if (vo != null) {
			dataSource = new DataSource();
			BeanUtils.copyProperties(vo, dataSource);
		}
		return dataSource;
	}

	private KPI toKPIJson(KPIVO vo) {
		KPI kpi = null;
		if (vo != null) {
			kpi = new KPI();
			BeanUtils.copyProperties(vo, kpi);
		}
		return kpi;
	}

	private InternalCustomer toInternalCustomerJson(InternalCustomerVO vo) {
		InternalCustomer internalCustomer = null;
		if (vo != null) {
			internalCustomer = new InternalCustomer();
			BeanUtils.copyProperties(vo, internalCustomer);			
			internalCustomer.setProcessOwner(toTeamMemberJson(vo.getProcessOwner()));
			internalCustomer.setDivision(toDivisionJson(vo.getDivision()));
		}
		return internalCustomer;
	}

	private ExternalCustomer toExternalCustomerJson(ExternalCustomerVO vo) {
		ExternalCustomer externalCustomer = null;
		if (vo != null) {
			externalCustomer = new ExternalCustomer();
			BeanUtils.copyProperties(vo, externalCustomer);
		}
		return externalCustomer;
	}

	private Division toDivisionJson(DivisionVO vo) {
		Division division = null;
		if (vo != null) {
			division = new Division();
			BeanUtils.copyProperties(vo, division);
			Subdivision subdivision = new Subdivision();
			if (vo.getSubdivision() != null) {
				BeanUtils.copyProperties(vo.getSubdivision(), subdivision);
			}
			division.setSubdivision(subdivision);
		}
		return division;
	}

	private TeamMember toTeamMemberJson(TeamMemberVO vo) {
		TeamMember teamMember = null;
		if (vo != null) {
			teamMember = new TeamMember();
			BeanUtils.copyProperties(vo, teamMember);
			if (vo.getUserType() != null) {
				teamMember.setUserType(vo.getUserType().name());
			}
		}
		return teamMember;
	}

	public List<String> toList(String parameter) {
		List<String> results = null;
		if (StringUtils.hasText(parameter)) {
			results = new ArrayList<String>();
			String[] parameters = parameter.split(",");
			if (!ObjectUtils.isEmpty(parameters))
				results = Arrays.asList(parameters);
		}
		return results;
	}
}
