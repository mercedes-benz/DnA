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

package com.daimler.data.service.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.ReportAssembler;
import com.daimler.data.auth.client.DnaAuthClient;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.jsonb.report.CustomerDetails;
import com.daimler.data.db.jsonb.report.DataWarehouse;
import com.daimler.data.db.jsonb.report.Division;
import com.daimler.data.db.jsonb.report.KPI;
import com.daimler.data.db.jsonb.report.SingleDataSource;
import com.daimler.data.db.jsonb.report.Subdivision;
import com.daimler.data.db.repo.report.ReportCustomRepository;
import com.daimler.data.db.repo.report.ReportRepository;
import com.daimler.data.dto.datawarehouse.DataWarehouseInUseVO;
import com.daimler.data.dto.department.DepartmentVO;
import com.daimler.data.dto.divisions.DivisionReportVO;
import com.daimler.data.dto.report.CreatedByVO;
import com.daimler.data.dto.report.CustomerVO;
import com.daimler.data.dto.report.MemberVO;
import com.daimler.data.dto.report.ProcessOwnerCollection;
import com.daimler.data.dto.report.ProductOwnerCollection;
import com.daimler.data.dto.report.ReportResponseVO;
import com.daimler.data.dto.report.ReportVO;
import com.daimler.data.dto.report.SubdivisionVO;
import com.daimler.data.dto.report.TeamMemberVO;
import com.daimler.data.dto.solution.UserInfoVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.department.DepartmentService;
import com.daimler.data.service.tag.TagService;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import io.jsonwebtoken.lang.Strings;

@Service
@SuppressWarnings(value = "unused")
public class BaseReportService extends BaseCommonService<ReportVO, ReportNsql, String> implements ReportService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseReportService.class);

	@Autowired
	private TagService tagService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private KafkaProducerService kafkaProducer;

	private ReportAssembler reportAssembler;

	private ReportCustomRepository reportCustomRepository;

	private ReportRepository reportRepository;

	@Autowired
	private DnaAuthClient dnaAuthClient;

	public BaseReportService() {
		super();
	}

	@Autowired
	public BaseReportService(ReportRepository reportRepository, ReportAssembler reportAssembler,
			ReportCustomRepository reportCustomRepository) {
		super(reportRepository, reportAssembler, reportCustomRepository);
		this.reportAssembler = reportAssembler;
		this.reportCustomRepository = reportCustomRepository;
		this.reportRepository = reportRepository;
	}

	@Override
	@Transactional
	public ReportVO create(ReportVO vo) {
		updateTags(vo);
		updateDepartments(vo);
		return super.create(vo);
	}

	@Override
	@Transactional
	public ReportVO getById(String id) {
		if (StringUtils.hasText(id)) {
			ReportVO existingVO = super.getByUniqueliteral("reportId", id);
			if (existingVO != null && existingVO.getReportId() != null) {
				return existingVO;
			} else {
				return super.getById(id);
			}
		}
		return null;
	}

	@Override
	public List<ReportVO> getAllWithFilters(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy, String sortOrder,
			String division, List<String> department, List<String> processOwner, List<String> productOwner,
			List<String> art) {
		List<ReportNsql> reportEntities = reportCustomRepository.getAllWithFiltersUsingNativeQuery(published, statuses,
				userId, isAdmin, searchTerms, tags, offset, limit, sortBy, sortOrder, division, department,
				processOwner, productOwner, art);
		if (!ObjectUtils.isEmpty(reportEntities))
			return reportEntities.stream().map(n -> reportAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, String division, List<String> department,
			List<String> processOwner, List<String> productOwner, List<String> art) {
		return reportCustomRepository.getCountUsingNativeQuery(published, statuses, userId, isAdmin, searchTerms, tags,
				division, department, processOwner, productOwner, art);
	}

	@Override
	@Transactional
	public void deleteForEachReport(String name, CATEGORY category) {
		List<ReportNsql> reports = null;
		if (StringUtils.hasText(name)) {
			reports = reportCustomRepository.getAllWithFiltersUsingNativeQuery(null, null, null, true,
					Arrays.asList(name), null, 0, 0, null, null, null, null, null, null, null);
		}
		if (!ObjectUtils.isEmpty(reports)) {
			reports.forEach(reportNsql -> {
				if (category.equals(CATEGORY.TAG)) {
					List<String> tags = reportNsql.getData().getDescription().getTags();
					if (!ObjectUtils.isEmpty(tags)) {
						Iterator<String> itr = tags.iterator();
						while (itr.hasNext()) {
							String tag = itr.next();
							if (tag.equals(name)) {
								itr.remove();
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.DEPARTMENT)) {
					String department = reportNsql.getData().getDescription().getDepartment();
					if (StringUtils.hasText(department) && department.equals(name)) {
						reportNsql.getData().getDescription().setDepartment(null);
					}
				} else if (category.equals(CATEGORY.INTEGRATED_PORTAL)) {
					List<String> integratedPortals = reportNsql.getData().getDescription().getIntegratedPortal();
					if (!ObjectUtils.isEmpty(integratedPortals)) {
						Iterator<String> itr = integratedPortals.iterator();
						while (itr.hasNext()) {
							String integratedPortal = itr.next();
							if (integratedPortal.equals(name)) {
								itr.remove();
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.FRONTEND_TECH)) {
					List<String> frontendTechnologies = reportNsql.getData().getDescription().getFrontendTechnologies();
					if (!ObjectUtils.isEmpty(frontendTechnologies)) {
						Iterator<String> itr = frontendTechnologies.iterator();
						while (itr.hasNext()) {
							String frontendTechnology = itr.next();
							if (frontendTechnology.equals(name)) {
								itr.remove();
								break;
							}
						}

					}
				} else if (category.equals(CATEGORY.ART)) {
					List<String> arts = reportNsql.getData().getDescription().getAgileReleaseTrains();
					if (!ObjectUtils.isEmpty(arts)) {
						Iterator<String> itr = arts.iterator();
						while (itr.hasNext()) {
							String art = itr.next();
							if (art.equals(name)) {
								itr.remove();
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.STATUS)) {
					String status = reportNsql.getData().getDescription().getStatus();
					if (StringUtils.hasText(status) && status.equals(name)) {
						reportNsql.getData().getDescription().setStatus(null);
					}
				} else if (category.equals(CATEGORY.CUST_DEPARTMENT)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getDepartment())
									&& customer.getDepartment().equals(name)) {
								customer.setDepartment(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.HIERARCHIES)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getHierarchy()) && customer.getHierarchy().equals(name)) {
								customer.setHierarchy(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.RESSORT)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getRessort()) && customer.getRessort().equals(name)) {
								customer.setRessort(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.KPI_NAME)) {
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							if (StringUtils.hasText(kpi.getName()) && kpi.getName().equals(name)) {
								kpi.setName(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.REPORTING_CAUSE)) {
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							if (StringUtils.hasText(kpi.getReportingCause()) && kpi.getReportingCause().equals(name)) {
								kpi.setReportingCause(null);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATASOURCE)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> dataSources = singleDataSource.getDataSources();
							if (!ObjectUtils.isEmpty(dataSources)) {
								Iterator<String> itr = dataSources.iterator();
								while (itr.hasNext()) {
									String dataSource = itr.next();
									if (dataSource.equals(name)) {
										itr.remove();
										break;
									}
								}
							}
						}
					}

				} else if (category.equals(CATEGORY.CONNECTION_TYPE)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> connectionTypes = singleDataSource.getConnectionTypes();
							if (!ObjectUtils.isEmpty(connectionTypes)) {
								Iterator<String> itr = connectionTypes.iterator();
								while (itr.hasNext()) {
									String connectionType = itr.next();
									if (connectionType.equals(name)) {
										itr.remove();
										break;
									}
								}
							}
						}
					}
				} else if (category.equals(CATEGORY.SUBSYSTEM)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> subsystems = singleDataSource.getSubsystems();
							if (!ObjectUtils.isEmpty(subsystems)) {
								Iterator<String> itr = subsystems.iterator();
								while (itr.hasNext()) {
									String subsytem = itr.next();
									if (subsytem.equals(name)) {
										itr.remove();
										break;
									}
								}
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_WAREHOUSE)) {
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						Iterator<DataWarehouse> itr = dataWarehouses.iterator();
						while (itr.hasNext()) {
							DataWarehouse dw = itr.next();
							if (StringUtils.hasText(dw.getDataWarehouse()) && dw.getDataWarehouse().equals(name)) {
								itr.remove();
							}
						}
					}
				} else if (category.equals(CATEGORY.DIVISION)) {
					Division reportdivision = reportNsql.getData().getDescription().getDivision();
					if (Objects.nonNull(reportdivision) && StringUtils.hasText(reportdivision.getId())
							&& reportdivision.getId().equals(name)) {
						reportdivision.setName(null);
						reportdivision.setId(null);
						reportdivision.setSubdivision(null);
					}
				}
				reportCustomRepository.update(reportNsql);

			});
		}
	}

	@Override
	@Transactional
	public void updateForEachReport(String oldValue, String newValue, CATEGORY category, Object updateObject) {
		List<ReportNsql> reports = null;
		if (StringUtils.hasText(oldValue)) {
			reports = reportCustomRepository.getAllWithFiltersUsingNativeQuery(null, null, null, true,
					Arrays.asList(oldValue), null, 0, 0, null, null, null, null, null, null, null);
		}
		if (!ObjectUtils.isEmpty(reports)) {
			reports.forEach(reportNsql -> {
				if (category.equals(CATEGORY.TAG)) {
					List<String> tags = reportNsql.getData().getDescription().getTags();
					if (!ObjectUtils.isEmpty(tags)) {
						ListIterator<String> itr = tags.listIterator();
						while (itr.hasNext()) {
							String tag = itr.next();
							if (tag.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.DEPARTMENT)) {
					String department = reportNsql.getData().getDescription().getDepartment();
					if (StringUtils.hasText(department) && department.equals(oldValue)) {
						reportNsql.getData().getDescription().setDepartment(newValue);
					}
				} else if (category.equals(CATEGORY.INTEGRATED_PORTAL)) {
					List<String> integratedPortals = reportNsql.getData().getDescription().getIntegratedPortal();
					if (!ObjectUtils.isEmpty(integratedPortals)) {
						ListIterator<String> itr = integratedPortals.listIterator();
						while (itr.hasNext()) {
							String integratedPortal = itr.next();
							if (integratedPortal.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.FRONTEND_TECH)) {
					List<String> frontendTechnologies = reportNsql.getData().getDescription().getFrontendTechnologies();
					if (!ObjectUtils.isEmpty(frontendTechnologies)) {
						ListIterator<String> itr = frontendTechnologies.listIterator();
						while (itr.hasNext()) {
							String frontendTechnology = itr.next();
							if (frontendTechnology.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.ART)) {
					List<String> arts = reportNsql.getData().getDescription().getAgileReleaseTrains();
					if (!ObjectUtils.isEmpty(arts)) {
						ListIterator<String> itr = arts.listIterator();
						while (itr.hasNext()) {
							String art = itr.next();
							if (art.equals(oldValue)) {
								itr.set(newValue);
								break;
							}
						}
					}
				} else if (category.equals(CATEGORY.STATUS)) {
					String status = reportNsql.getData().getDescription().getStatus();
					if (StringUtils.hasText(status) && status.equals(oldValue)) {
						reportNsql.getData().getDescription().setStatus(newValue);
					}
				} else if (category.equals(CATEGORY.CUST_DEPARTMENT)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getDepartment())
									&& customer.getDepartment().equals(oldValue)) {
								customer.setDepartment(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.HIERARCHIES)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getHierarchy())
									&& customer.getHierarchy().equals(oldValue)) {
								customer.setHierarchy(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.RESSORT)) {
					List<CustomerDetails> customers = reportNsql.getData().getCustomer().getCustomers();
					if (!ObjectUtils.isEmpty(customers)) {
						for (CustomerDetails customer : customers) {
							if (StringUtils.hasText(customer.getRessort()) && customer.getRessort().equals(oldValue)) {
								customer.setRessort(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.KPI_NAME)) {
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							if (StringUtils.hasText(kpi.getName()) && kpi.getName().equals(oldValue)) {
								kpi.setName(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.REPORTING_CAUSE)) {
					List<KPI> kpis = reportNsql.getData().getKpis();
					if (!ObjectUtils.isEmpty(kpis)) {
						for (KPI kpi : kpis) {
							if (StringUtils.hasText(kpi.getReportingCause())
									&& kpi.getReportingCause().equals(oldValue)) {
								kpi.setReportingCause(newValue);
							}
						}
					}
				} else if (category.equals(CATEGORY.DATASOURCE)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> dataSources = singleDataSource.getDataSources();
							if (!ObjectUtils.isEmpty(dataSources)) {
								ListIterator<String> itr = dataSources.listIterator();
								while (itr.hasNext()) {
									String dataSource = itr.next();
									if (dataSource.equals(oldValue)) {
										itr.set(newValue);
										break;
									}
								}
							}
						}
					}

				} else if (category.equals(CATEGORY.CONNECTION_TYPE)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> connectionTypes = singleDataSource.getConnectionTypes();
							if (!ObjectUtils.isEmpty(connectionTypes)) {
								ListIterator<String> itr = connectionTypes.listIterator();
								while (itr.hasNext()) {
									String connectionType = itr.next();
									if (connectionType.equals(oldValue)) {
										itr.set(newValue);
										break;
									}
								}
							}
						}
					}
				} else if (category.equals(CATEGORY.SUBSYSTEM)) {
					List<SingleDataSource> singleDataSources = reportNsql.getData().getSingleDataSources();
					if (!ObjectUtils.isEmpty(singleDataSources)) {
						for (SingleDataSource singleDataSource : singleDataSources) {
							List<String> subsystems = singleDataSource.getSubsystems();
							if (!ObjectUtils.isEmpty(subsystems)) {
								ListIterator<String> itr = subsystems.listIterator();
								while (itr.hasNext()) {
									String subsytem = itr.next();
									if (subsytem.equals(oldValue)) {
										itr.set(newValue);
										break;
									}
								}
							}
						}
					}
				} else if (category.equals(CATEGORY.DATA_WAREHOUSE)) {
					// updating data warehouse in each reports
					List<DataWarehouse> dataWarehouses = reportNsql.getData().getDataWarehouses();
					DataWarehouseInUseVO dataWarehouseInUseVO = (DataWarehouseInUseVO) updateObject;
					if (!ObjectUtils.isEmpty(dataWarehouses)) {
						for (DataWarehouse dataWarehouse : dataWarehouses) {
							if (StringUtils.hasText(dataWarehouse.getDataWarehouse())
									&& dataWarehouse.getDataWarehouse().equals(oldValue)) {
								dataWarehouse.setDataWarehouse(newValue);
								// common function
								List<String> commonFunctions = dataWarehouse.getCommonFunctions();
								if (!ObjectUtils.isEmpty(commonFunctions)) {
									Iterator<String> itr = commonFunctions.iterator();
									while (itr.hasNext()) {
										String value = itr.next();
										if (Objects.nonNull(dataWarehouseInUseVO.getCommonFunctions())
												&& !dataWarehouseInUseVO.getCommonFunctions().contains(value)) {
											itr.remove();
										}
									}
								}
								// specific function
								List<String> specificFunctions = dataWarehouse.getSpecificFunctions();
								if (!ObjectUtils.isEmpty(specificFunctions)) {
									Iterator<String> itr = specificFunctions.iterator();
									while (itr.hasNext()) {
										String value = itr.next();
										if (Objects.nonNull(dataWarehouseInUseVO.getSpecificFunctions())
												&& !dataWarehouseInUseVO.getSpecificFunctions().contains(value)) {
											itr.remove();
										}
									}
								}
								// queries
								List<String> queries = dataWarehouse.getQueries();
								if (!ObjectUtils.isEmpty(queries)) {
									Iterator<String> itr = queries.iterator();
									while (itr.hasNext()) {
										String value = itr.next();
										if (Objects.nonNull(dataWarehouseInUseVO.getQueries())
												&& !dataWarehouseInUseVO.getQueries().contains(value)) {
											itr.remove();
										}
									}
								}
								// Data sources
								List<String> dataSources = dataWarehouse.getDataSources();
								if (!ObjectUtils.isEmpty(dataSources)) {
									Iterator<String> itr = dataSources.iterator();
									while (itr.hasNext()) {
										String value = itr.next();
										if (Objects.nonNull(dataWarehouseInUseVO.getDataSources())
												&& !dataWarehouseInUseVO.getDataSources().contains(value)) {
											itr.remove();
										}
									}
								}
								// Connection type
								List<String> connectionTypes = dataWarehouse.getConnectionTypes();
								if (!ObjectUtils.isEmpty(connectionTypes)) {
									Iterator<String> itr = connectionTypes.iterator();
									while (itr.hasNext()) {
										String value = itr.next();
										if (Objects.nonNull(dataWarehouseInUseVO.getConnectionTypes())
												&& !dataWarehouseInUseVO.getConnectionTypes().contains(value)) {
											itr.remove();
										}
									}
								}
							}
						}
					}
				} else if (category.equals(CATEGORY.DIVISION)) {
					Division reportdivision = reportNsql.getData().getDescription().getDivision();
					DivisionReportVO divisionVO = (DivisionReportVO) updateObject;
					if (Objects.nonNull(reportdivision) && StringUtils.hasText(reportdivision.getId())
							&& reportdivision.getId().equals(divisionVO.getId())) {
						reportdivision.setName(divisionVO.getName().toUpperCase());
						Subdivision subdivision = reportdivision.getSubdivision();
						List<SubdivisionVO> subdivisionlist = divisionVO.getSubdivisions();
						if (Objects.nonNull(subdivision)) {
							if (ObjectUtils.isEmpty(subdivisionlist)) {
								reportdivision.setSubdivision(null);
							} else {
								boolean exists = false;
								for (SubdivisionVO value : subdivisionlist) {
									if (StringUtils.hasText(value.getId())
											&& value.getId().equals(subdivision.getId())) {
										Subdivision subdiv = new Subdivision();
										subdiv.setId(value.getId());
										subdiv.setName(value.getName().toUpperCase());
										reportdivision.setSubdivision(subdiv);
										exists = true;
										break;
									}
								}
								if (!exists) {
									reportdivision.setSubdivision(null);
								}
							}
						}
					}
				}
				reportCustomRepository.update(reportNsql);
			});
		}
	}

	private void updateTags(ReportVO vo) {
		List<String> tags = vo.getDescription().getTags();
		if (tags != null && !tags.isEmpty()) {
			tags.forEach(tag -> {
				TagVO existingTagVO = tagService.findTagByName(tag);
				if (existingTagVO != null && existingTagVO.getName() != null)
					return;
				else {
					TagVO newTagVO = new TagVO();
					newTagVO.setName(tag);
					tagService.create(newTagVO);
				}
			});
		}
	}

	private void updateDepartments(ReportVO vo) {
		String department = vo.getDescription().getDepartment();
		if (Strings.hasText(department)) {
			DepartmentVO existingDepartmentVO = departmentService.findDepartmentByName(department);
			if (existingDepartmentVO != null && existingDepartmentVO.getName() != null)
				return;
			else {
				DepartmentVO newDepartmentVO = new DepartmentVO();
				newDepartmentVO.setName(department);
				departmentService.create(newDepartmentVO);
			}

		}
	}

	@Override
	@Transactional
	public ResponseEntity<ReportResponseVO> createReport(ReportVO requestReportVO) {
		ReportResponseVO reportResponseVO = new ReportResponseVO();
		try {
			String uniqueProductName = requestReportVO.getProductName();
			ReportVO existingReportVO = super.getByUniqueliteral("productName", uniqueProductName);
			if (existingReportVO != null && existingReportVO.getProductName() != null) {
				reportResponseVO.setData(existingReportVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Report already exists.");
				messages.add(message);
				reportResponseVO.setErrors(messages);
				LOGGER.info("Report {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.CONFLICT);
			}
			requestReportVO.setCreatedBy(this.userStore.getVO());
			requestReportVO.setCreatedDate(new Date());
			requestReportVO.setId(null);
			requestReportVO.setReportId("REP-" + String.format("%05d", reportRepository.getNextSeqId()));
			if (requestReportVO.isPublish() == null)
				requestReportVO.setPublish(false);

			ReportVO reportVO = this.create(requestReportVO);
			if (reportVO != null && reportVO.getId() != null) {
				reportResponseVO.setData(reportVO);
				LOGGER.info("Report {} created successfully", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				reportResponseVO.setData(requestReportVO);
				reportResponseVO.setErrors(messages);
				LOGGER.error("Report {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(reportResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating report {} ", e.getMessage(),
					requestReportVO.getProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			reportResponseVO.setData(requestReportVO);
			reportResponseVO.setErrors(messages);
			return new ResponseEntity<>(reportResponseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ReportResponseVO> updateReport(ReportVO requestReportVO) {
		ReportResponseVO response = new ReportResponseVO();
		try {
			String id = requestReportVO.getId();
			ReportVO existingReportVO = super.getById(id);
			ReportVO mergedReportVO = null;
			if (requestReportVO.isPublish() == null) {
				requestReportVO.setPublish(false);
			}
			if (existingReportVO != null && existingReportVO.getId() != null) {
				CreatedByVO createdBy = existingReportVO.getCreatedBy();
				if (canProceedToEdit(existingReportVO)) {
					requestReportVO.setCreatedBy(createdBy);
					requestReportVO.setCreatedDate(existingReportVO.getCreatedDate());
					requestReportVO.lastModifiedDate(new Date());
					requestReportVO.setReportId(existingReportVO.getReportId());
					mergedReportVO = this.create(requestReportVO);
					if (mergedReportVO != null && mergedReportVO.getId() != null) {
						response.setData(mergedReportVO);
						response.setErrors(null);
						LOGGER.info("Report with id {} updated successfully", id);
						try {
							if (mergedReportVO.isPublish()) {
								CreatedByVO modifyingUser = this.userStore.getVO();
								String eventType = "Dashboard-Report Update";
								String resourceID = mergedReportVO.getId();
								String reportName = mergedReportVO.getProductName();
								String publishingUserId = "dna_system";
								String publishingUserName = "";
								if (modifyingUser != null) {
									publishingUserId = modifyingUser.getId();
									publishingUserName = modifyingUser.getFirstName() + " "
											+ modifyingUser.getLastName();
									if (publishingUserName == null || "".equalsIgnoreCase(publishingUserName))
										publishingUserName = publishingUserId;
								}
								List<String> membersId = new ArrayList<>();
								List<String> membersEmail = new ArrayList<>();
								MemberVO memberVO = mergedReportVO.getMembers();

								List<TeamMemberVO> members = new ArrayList<>();
								members.addAll(memberVO.getAdmin());
								members.addAll(memberVO.getDevelopers());
								members.addAll(memberVO.getProductOwners());
								CustomerVO customerVO = mergedReportVO.getCustomer();
								if (customerVO != null && customerVO.getProcessOwners() != null)
									members.addAll(customerVO.getProcessOwners());
								for (TeamMemberVO member : members) {
									if (member != null) {
										String memberId = member.getShortId() != null ? member.getShortId() : "";
										if (!membersId.contains(memberId)) {
											membersId.add(memberId);
											String emailId = member.getEmail() != null ? member.getEmail() : "";
											membersEmail.add(emailId);
										}
									}
								}
								String eventMessage = "Dashboard report " + reportName + " has been updated by "
										+ publishingUserName;
								kafkaProducer.send(eventType, resourceID, "", publishingUserId, eventMessage, true,
										membersId, membersEmail, null);
								LOGGER.info("Published successfully event {} for report {} with message {}", eventType,
										resourceID, eventMessage);
							}
						} catch (Exception e) {
							LOGGER.error("Failed while publishing dashboard report update event msg. Exception is {} ",
									e.getMessage());
						}

						return new ResponseEntity<>(response, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						response.setData(requestReportVO);
						response.setErrors(messages);
						LOGGER.info("Report with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage(
							"Not authorized to edit Report. Only user who created the Report or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					response.setErrors(notAuthorizedMsgs);
					LOGGER.info("Report with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No Report found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				response.setErrors(notFoundmessages);
				LOGGER.info("No Report found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("Report with id {} cannot be edited. Failed due to internal error {} ",
					requestReportVO.getId(), e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			response.setData(requestReportVO);
			response.setErrors(messages);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/*
	 * To check if user has access to proceed with edit or delete of the record.
	 * 
	 */
	private boolean canProceedToEdit(ReportVO existingReportVO) {
		boolean canProceed = false;
		boolean hasAdminAccess = this.userStore.getUserInfo().hasAdminAccess();
		// To fetch user info from dna-backend by id
		UserInfoVO userInfoVO = dnaAuthClient.userInfoById(this.userStore.getUserInfo().getId());
		// To check if user having DivisionAdmin role and division is same as Report
		boolean isDivisionAdmin = userInfoVO.getRoles().stream()
				.anyMatch(n -> ConstantsUtility.DIVISION_ADMIN.equals(n.getName()))
				&& userInfoVO.getDivisionAdmins().contains(existingReportVO.getDescription().getDivision().getName());
		if (hasAdminAccess) {
			canProceed = true;
		} else if (isDivisionAdmin) {
			canProceed = true;
		} else {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			boolean isTeamMember = false;
			boolean isProductOwner = false;
			if (StringUtils.hasText(userId)) {
				// To check if user is admin(Team member)
				isTeamMember = (existingReportVO.getMembers() != null
						&& !ObjectUtils.isEmpty(existingReportVO.getMembers().getAdmin()))
								? existingReportVO.getMembers().getAdmin().stream()
										.anyMatch(n -> userId.equalsIgnoreCase(n.getShortId()))
								: false;
				// To check if user is Product Owner
				isProductOwner = (existingReportVO.getMembers() != null
						&& !ObjectUtils.isEmpty(existingReportVO.getMembers().getProductOwners()))
								? existingReportVO.getMembers().getProductOwners().stream().anyMatch(
										n -> userId.equalsIgnoreCase(n.getShortId()))
								: false;
			}
			if (isTeamMember || isProductOwner) {
				canProceed = true;
			}
		}

		return canProceed;
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteReport(String id) {
		try {
			ReportVO report = super.getById(id);
			if (canProceedToEdit(report)) {
				this.deleteById(id);
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("Report with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete Report. Only the Report owner or an admin can delete the Report.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				LOGGER.debug("Report with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No Report with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No Report with the given id {} , could not delete.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete report with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ProcessOwnerCollection> getProcessOwners() {
		ProcessOwnerCollection processOwnerCollection = new ProcessOwnerCollection();
		try {
			List<TeamMemberVO> processOwnerList = reportCustomRepository.getAllProcessOwnerUsingNativeQuery();
			LOGGER.debug("ProcessOwners fetched successfully");
			if (!ObjectUtils.isEmpty(processOwnerList)) {
				processOwnerCollection.setRecords(processOwnerList);
				return new ResponseEntity<>(processOwnerCollection, HttpStatus.OK);
			} else
				return new ResponseEntity<>(processOwnerCollection, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch processOwners with exception {} ", e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<ProductOwnerCollection> getProductOwners() {
		ProductOwnerCollection productOwnerCollection = new ProductOwnerCollection();
		try {
			List<TeamMemberVO> productOwnerList = reportCustomRepository.getAllProductOwnerUsingNativeQuery();
			LOGGER.debug("ProductOwners fetched successfully");
			if (!ObjectUtils.isEmpty(productOwnerList)) {
				productOwnerCollection.setRecords(productOwnerList);
				return new ResponseEntity<>(productOwnerCollection, HttpStatus.OK);
			} else
				return new ResponseEntity<>(productOwnerCollection, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch productOwners with exception {} ", e.getMessage());
			throw e;
		}
	}

}
