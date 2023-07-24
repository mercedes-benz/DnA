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

package com.daimler.data.db.repo.dataproduct;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import com.daimler.data.application.client.DashboardServicesClient;
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CarlaFunctionNsql;
import com.daimler.data.dto.dataproduct.DataProductTeamMemberVO;
import com.daimler.data.dto.userinfo.dashboard.DashboardServiceLovDto;
import com.daimler.data.dto.userinfo.dashboard.GetDashboardServiceLovResponseWrapperDto;
import com.daimler.data.util.ConstantsUtility;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.jsonb.dataproduct.DataProduct;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class DataProductCustomRepositoryImpl extends CommonDataRepositoryImpl<DataProductNsql, String>
		implements DataProductCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DataProductCustomRepositoryImpl.class);
	private static String REGEX = "[\\[+\\]+:{}^~?\\\\/()><=\"!]";

	@Autowired
	private DashboardServicesClient dashboardServicesClient;

	@Autowired
	private DataProductAssembler dataProductAssembler;

	@Transactional
	@Override
	public GenericMessage updateDataProductData() {
		GenericMessage genericMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<DataProductNsql> cq = cb.createQuery(DataProductNsql.class);
			Root<DataProductNsql> root = cq.from(DataProductNsql.class);
			TypedQuery<DataProductNsql> typedQuery = em.createQuery(cq);
			List<DataProductNsql> dataproductResults = typedQuery.getResultList();

			CriteriaQuery<CarlaFunctionNsql> cq1 = cb.createQuery(CarlaFunctionNsql.class);
			Root<CarlaFunctionNsql> root1 = cq1.from(CarlaFunctionNsql.class);
			TypedQuery<CarlaFunctionNsql> carlatypedQuery = em.createQuery(cq1);
			List<CarlaFunctionNsql> carlaResults = carlatypedQuery.getResultList();

			GetDashboardServiceLovResponseWrapperDto agileReleaseTrainsResp = dashboardServicesClient.getAgileReleaseTrain();

			if (dataproductResults != null && !dataproductResults.isEmpty()) {
				for (DataProductNsql entity : dataproductResults) {

					if (entity.getData() != null) {
						// Below condition is used for adding id if it's missed in carla Lov of dataProduct.
						if (entity.getData().getCarLaFunction() != null) {
							if (entity.getData().getCarLaFunction().getId() == null) {
								String id = null;

								// To get individual carlaResults.
								if (carlaResults != null && !carlaResults.isEmpty()) {
									for (CarlaFunctionNsql carla : carlaResults) {
										if (entity.getData().getCarLaFunction().getName().equals(carla.getData().getName())) {
											id = carla.getId();
											LOGGER.info("carla functions with id: {} and name {}", carla.getId(), carla.getData().getName());
										}
									}
								}
								if (id != null) {
									entity.getData().getCarLaFunction().setId(id);
									LOGGER.info("Updating dataproduct's carlafunctionLov with dataProduct id: {} and CarlaFunction {}", entity.getId(), entity.getData().getCarLaFunction().getName());
								}
								try {
									em.merge(entity);
								} catch (Exception e) {
									MessageDescription errMsg = new MessageDescription("failed to update the carla functions for dataproduct id: " + entity.getId() + " with an Exception " + e.getMessage());
									errors.add(errMsg);
									LOGGER.error(errMsg.getMessage());
								}
							}
						}

						// Below condition is used for adding id if it's missed in AgileReleseTrain Lov of dataProduct.
						if (entity.getData().getAgileReleaseTrain() != null) {
							if (entity.getData().getAgileReleaseTrain().getId() == null) {
								String id = null;

								// To get individual agileReleaseTrainsResp.
								if (agileReleaseTrainsResp != null && agileReleaseTrainsResp.getData() != null && !agileReleaseTrainsResp.getData().isEmpty()) {
									for (DashboardServiceLovDto art : agileReleaseTrainsResp.getData()) {
										if (entity.getData().getAgileReleaseTrain().getName().equals(art.getName())) {
											id = art.getId();
											LOGGER.info("AgileReleaseTrain functions with id: {} and name {}", art.getId(), art.getName());
										}
									}
								}
								if (id != null) {
									entity.getData().getAgileReleaseTrain().setId(id);
									LOGGER.info("Updating dataproduct's AgileReleaseTrainLov with dataProduct id: {} and AgileReleaseTrain {}", entity.getId(), entity.getData().getAgileReleaseTrain().getName());
								}
								try {
									em.merge(entity);
								} catch (Exception e) {
									MessageDescription errMsg = new MessageDescription("failed to update the AgileReleaseTrain functions for dataproduct id: " + entity.getId() + " with an Exception " + e.getMessage());
									errors.add(errMsg);
									LOGGER.error(errMsg.getMessage());
								}
							}
						}

						// Below condition is used for setting up the values of contactAwareTransfer as false for personal related data.
						if (entity.getData().getPersonalRelatedData() != null) {
							if (entity.getData().getPersonalRelatedData().isPersonalRelatedData()) {
								if (!entity.getData().getPersonalRelatedData().isContactAwareTransfer()) {
									entity.getData().getPersonalRelatedData().setContactAwareTransfer(false);
									entity.getData().setLastModifiedDate(new Date());
									try {
										em.merge(entity);
									} catch (Exception e) {
										MessageDescription errMsg = new MessageDescription("failed to update the getPersonalRelatedData for dataproduct id: " + entity.getId() + " with an Exception " + e.getMessage());
										errors.add(errMsg);
										LOGGER.error(errMsg.getMessage());
									}
								}
							}
						}

						// Below condition is used for setting up the values of contactAwareTransfer as false for personal related data.
						if (entity.getData().getTransnationalDataTransfer() != null) {
							if (entity.getData().getTransnationalDataTransfer().isDataTransferred()) {
								if (entity.getData().getTransnationalDataTransfer().isNotWithinEU()) {
									if (!entity.getData().getTransnationalDataTransfer().isContactAwareTransfer()) {
										entity.getData().getTransnationalDataTransfer().setContactAwareTransfer(false);
										entity.getData().setLastModifiedDate(new Date());
										try {
											em.merge(entity);
										} catch (Exception e) {
											MessageDescription errMsg = new MessageDescription("failed to update the getTransnationalDataTransfer for dataproduct id: " + entity.getId() + " with an Exception " + e.getMessage());
											errors.add(errMsg);
											LOGGER.error(errMsg.getMessage());
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			MessageDescription errMsg = new MessageDescription("Failed to update the data of dataProduct with an exception " + e.getMessage());
			errors.add(errMsg);
			LOGGER.error(errMsg.getMessage());
		}
		genericMessage.setErrors(errors);
		if (errors.size() > 0) {
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			return genericMessage;
		} else {
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
		}
		return genericMessage;
	}

	@Override
	public Integer getCountBasedPublishReport(Boolean published) {
		String query = "select count(*) from dataproduct_nsql where ((jsonb_extract_path_text(data,'publish')) = (" +"'"+ published +"'"+ "))";
		Query q = em.createNativeQuery(query);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.intValue();
	}

	@Override
	public List<DataProductNsql> getAllWithFiltersUsingNativeQuery(Boolean published, int offset, int limit,
			String sortBy, String sortOrder, String recordStatus,
			List<String> artsList, List<String> carlafunctionsList, List<String> platformsList, List<String> frontendToolsList,
			List<String> productOwnerList) {
		Query q = getNativeQueryWithFilters("", published, offset, limit, sortBy, sortOrder, recordStatus,
				artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = null;
		try {
			results = q.getResultList();
		}catch(Exception e){
			e.printStackTrace();
		}
		List<DataProductNsql> convertedResults = results.stream().map(temp -> {
			DataProductNsql entity = new DataProductNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				DataProduct tempDataProduct = mapper.readValue(jsonData, DataProduct.class);
				entity.setData(tempDataProduct);
			} catch (Exception e) {
				LOGGER.error("Failed to fetch dataproducts with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

	@Override
	public Long getCountUsingNativeQuery(Boolean published, String recordStatus,
		List<String> artsList, List<String> carlafunctionsList,
		List<String> platformsList, List<String> frontendToolsList, List<String> productOwnerList) {

		Query q = getNativeQueryWithFilters("select count(*) ", published, 0, 0, "", "asc", recordStatus,
				artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	@Override
	public List<DataProductTeamMemberVO> getOwnersAllWithFiltersUsingNativeQuery(Boolean published, int offset, int limit,
																				 String sortOrder, String recordStatus) {
		Query q = getNativeQueryWithFiltersForDataProductOwners("", published, offset, limit, sortOrder, recordStatus);
		List<String> resultsList = new ArrayList<>();
		List<DataProductTeamMemberVO> results = new ArrayList<>();
		resultsList = q.getResultList();
		if (resultsList != null && resultsList.size() > 0) {
			resultsList.forEach(val -> {
				DataProductTeamMemberVO vo = dataProductAssembler.toParseDataProductTeamMemberVO(val);
				if(vo!=null) {
					results.add(vo);
				}
			});
		}
		return results;
	}
	@Override
	public Long getCountOwnersUsingNativeQuery(Boolean published, String recordStatus) {

		Query q = getNativeQueryWithFiltersForDataProductOwners("SELECT COUNT(DISTINCT jsonb_extract_path_text(data, 'createdBy', 'id')) AS total_unique_created_by_ids ", published, 0, 0, "asc", recordStatus);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	private Query getNativeQueryWithFiltersForDataProductOwners(String selectFieldsString, Boolean published, int offset, int limit, String sortOrder, String recordStatus) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
//				: "SELECT DISTINCT jsonb_extract_path(data, 'createdBy') AS created_by_id ";
				: "SELECT DISTINCT jsonb_extract_path_text(data, 'createdBy') AS created_by_id ";
		prefix = prefix + "FROM dataproduct_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(published, recordStatus);
		String query = prefix + basicpredicate + consolidatedPredicates;
		String sortQueryString = "";
		if (StringUtils.hasText(sortOrder)) {
			String sortbyQueryValue =  prefix.split("AS ")[1].split(" ")[0];
			if ("desc".equalsIgnoreCase(sortOrder)) {
				sortQueryString =  "ORDER BY " + sortbyQueryValue + " desc ";
			} else {
				sortQueryString = "ORDER BY " + sortbyQueryValue + " asc ";
			}

			query = query + sortQueryString;
		}
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
			Query q = em.createNativeQuery(query);
		return q;
	}

	private Query getNativeQueryWithFilters(String selectFieldsString, Boolean published, int offset, int limit,
			String sortBy, String sortOrder, String recordStatus,
			List<String> artsList, List<String> carlafunctionsList,
			List<String> platformsList, List<String> frontendToolsLis,
			List<String> productOwnerList) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from dataproduct_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(
				published, recordStatus,
				artsList, carlafunctionsList,
				platformsList, frontendToolsLis,
				productOwnerList);
		String query = prefix + basicpredicate + consolidatedPredicates;
		String sortQueryString = "";
		if (StringUtils.hasText(sortBy)) {
			switch (sortBy) {
			case "dataProductName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'dataProductName')) ";
				break;
			case "dataProductId":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'dataProductId')) ";
				break;				
			default:
				sortQueryString = "";
				break;
			}
			if ("desc".equalsIgnoreCase(sortOrder))
				sortQueryString = sortQueryString + " desc ";
			else
				sortQueryString = sortQueryString + " asc ";

			query = query + sortQueryString;

		}
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
		Query q = em.createNativeQuery(query);
		return q;
	}

	private String buildPredicateString(Boolean published, String recordStatus,
		List<String> artsList, List<String> carlafunctionsList,
		List<String> platformsList, List<String> frontendToolsLis,
		List<String> productOwnerList) {
		return getArtPredicateString(artsList) + "\n" + getartCarlafunctionString(carlafunctionsList) + "\n"
				+ getPlatformsListPredicateString(platformsList) + "\n" + getartFrontendToolsString(frontendToolsLis) + "\n"
				+ getProductOwnerPredicateString(productOwnerList) + "\n"
				+ getPublishedAndAccessPredicate(published) + "\n" + getRecordStatusPredicateString(recordStatus);
	}

	private String buildPredicateString(Boolean published, String recordStatus) {
		return getPublishedAndAccessPredicate(published) + "\n" + getRecordStatusPredicateString(recordStatus);
	}

	private String getProductOwnerPredicateString(List<String> productOwnerList) {
		if (productOwnerList != null && !productOwnerList.isEmpty()) {
			String commaSeparatedProductOwner = productOwnerList.stream().map(s -> "%\"" + s + "\"%")
					.collect(Collectors.joining("|"));
			return " and (jsonb_extract_path_text(data, 'contactInformation', 'informationOwner') similar to '" + commaSeparatedProductOwner + "' )";
		}
		return "";
	}

	private String getArtPredicateString(List<String> artsList) {
		if (artsList != null && !artsList.isEmpty()) {
			String commaSeparatedArts = artsList.stream().map(s -> "%\"" + s + "\"%")
					.collect(Collectors.joining("|"));
			return " and (jsonb_extract_path_text(data, 'agileReleaseTrain') similar to '" + commaSeparatedArts + "' )";
		}
		return "";
	}

	private String getartCarlafunctionString(List<String> carlafunctionsList) {
		if (carlafunctionsList != null && !carlafunctionsList.isEmpty()) {
			String commaSeparatedCarlafunctions = carlafunctionsList.stream().map(s -> "%\"" + s + "\"%")
					.collect(Collectors.joining("|"));
			return " and (jsonb_extract_path_text(data, 'carLaFunction') similar to '" + commaSeparatedCarlafunctions + "' )";
		}
		return "";
	}

	private String getPlatformsListPredicateString(List<String> platformsList) {
		if (platformsList != null && !platformsList.isEmpty()) {
			String commaSeparatedPlatforms = platformsList.stream().map(s -> "%\"" + s + "\"%")
					.collect(Collectors.joining("|"));
			return " and (jsonb_extract_path_text(data, 'platform') similar to '" + commaSeparatedPlatforms + "' )";
		}
		return "";
	}

	private String getartFrontendToolsString(List<String> frontendToolsLis) {
		if (frontendToolsLis != null && !frontendToolsLis.isEmpty()) {
			String commaSeparatedFrontendTool = frontendToolsLis.stream().map(s -> "%\"" + s + "\"%")
					.collect(Collectors.joining("|"));
			return " and (jsonb_extract_path_text(data, 'frontEndTools') similar to '" + commaSeparatedFrontendTool + "' )";
		}
		return "";
	}


	private String getRecordStatusPredicateString(String recordStatus) {
		if (StringUtils.hasText(recordStatus)) {
			return " and (jsonb_extract_path_text(data,'recordStatus') in ('" + recordStatus + "')) ";
		}
		return "";
	}

	private String getPublishedAndAccessPredicate(Boolean published) {
		String allTrueCondition = " (jsonb_extract_path_text(data,'publish') in ('true')) ";
		String allFalseCondition = " (jsonb_extract_path_text(data,'publish') in ('false')) ";
		String query = "";

		if (published != null) {
			if (published) {
				query += " and " + allTrueCondition;
			} else if (!published) {
				query += " and " + allFalseCondition;
			}
		}

		return query;
	}

	/**
	 * To get Predicate string for division & subdivision combination.
	 *
	 * @param divisions
	 * @return divisionPredicate{String}
	 */
	private String getDivisionsPredicateString(String divisions) {
		if (StringUtils.hasText(divisions)) {
			StringBuilder consolidatedQuery = new StringBuilder();
			boolean isEmptySubdivision = false;
			// To remove outer [] bracket, eg: [{a,[1]},{b,[1]}], result:{a,[1]},{b,[1]}
			divisions = divisions.substring(1, divisions.length() - 1);
			// To Split value with <},>, eg: {a,[1]},{b,[1]}, result: {a,[1]} {b,[1]
			String[] divisionSplit = divisions.split("},", 0);
			if (divisions.contains("EMPTY")) {
				// If null has to be added in the subdivision predicate
				isEmptySubdivision = true;
			}
			for (String divSubdiv : divisionSplit) {
				String query = "";
				// To remove all the brackets
				divSubdiv = divSubdiv.replaceAll("[\\{\\}\\[\\]]", "");
				// To remove the trailing comma(,)
				divSubdiv = divSubdiv.replaceAll(",$", "");
				// To Split the string to get each division-subdivision combination with first
				// occurrence of comma(,)
				String[] divList = divSubdiv.trim().split(",", 2);
				if (divList.length > 1) {
					query += "(jsonb_extract_path_text(data, 'contactInformation', 'division', 'id') in ('"
							+ divList[0] + "')";
					String commaSeparatedinSubdivisions = Arrays.asList(divList[1].split(",")).stream()
							.collect(Collectors.joining("','", "'", "'"));
					if (isEmptySubdivision) {
						query += " and (jsonb_extract_path_text(data,'contactInformation', 'division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
						query += " or jsonb_extract_path_text(data, 'contactInformation','division','subdivision','id') is null)";
					} else {
						query += " and jsonb_extract_path_text(data, 'contactInformation','division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
					}
					query += ")";
				} else {
					query += "jsonb_extract_path_text(data,'contactInformation','division','id') in ('"
							+ divList[0] + "')";
				}
				if (divisionSplit.length > 1 && !consolidatedQuery.toString().isEmpty()) {
					consolidatedQuery.append(" or " + query);
				} else {
					consolidatedQuery.append(query);
				}
			}
			consolidatedQuery.insert(0, "and (");
			consolidatedQuery.append(")");
			return consolidatedQuery.toString();
		}
		return "";
	}
	
	public Query getNativeQueryWithFilters(String selectFieldsString, String uniqueProductName,String status) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) from";
		prefix = prefix + " dataproduct_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(uniqueProductName,status);
		String query = prefix + basicpredicate + consolidatedPredicate;		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
	private String buildPredicateString(String uniqueProductName, String status) {;
		if ((uniqueProductName != null && !uniqueProductName.isEmpty()) && (!status.isEmpty())) {			
			return " and (lower(jsonb_extract_path_text(data,'dataProductName')) in (" +"'"+ uniqueProductName.toLowerCase() +"'"+ "))" +  
					" and ((jsonb_extract_path_text(data,'recordStatus')) in (" +"'"+ status +"'"+ "))";
		}
		return "";
	}

	@Override
	public List<DataProductNsql> getExistingDataProduct(String uniqueProductName, String status) {
		Query q = getNativeQueryWithFilters("select cast (data as text) from ", uniqueProductName,status);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();				
		DataProductNsql entity = new DataProductNsql();
		List<DataProductNsql> dataProductNsqls = new ArrayList<>();
		if(results != null && !results.isEmpty()) {
			for(Object result : results) {
				try {
					String jsonData = result.toString() != null ? result.toString() : "";					
					DataProduct dataProduct = mapper.readValue(jsonData, DataProduct.class);
					entity.setData(dataProduct);
					dataProductNsqls.add(entity);
				}
				catch(Exception e) {
					LOGGER.error("Exception Occured: {}", e.getMessage());
				}
			}
		}
		return dataProductNsqls;				
	}
}
