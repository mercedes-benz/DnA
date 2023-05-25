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

package com.daimler.data.db.repo.report;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.jsonb.report.Report;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.report.InternalCustomerVO;
import com.daimler.data.dto.report.TeamMemberVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Repository
public class ReportCustomRepositoryImpl extends CommonDataRepositoryImpl<ReportNsql, String>
		implements ReportCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(ReportCustomRepositoryImpl.class);
	private static String REGEX = "[\\[+\\]+:{}^~?\\\\/()><=\"!]";
	
	@Value("${report.update}")
	private boolean updateReportData;
	
	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void updateData() {
		if (updateReportData) {
			try {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<ReportNsql> cq = cb.createQuery(ReportNsql.class);
				Root<ReportNsql> root = cq.from(ReportNsql.class);
				TypedQuery<ReportNsql> typedQuery = em.createQuery(cq);
				List<ReportNsql> results = typedQuery.getResultList();
				if (results != null && !results.isEmpty()) {
					for (ReportNsql entity : results) {
						Report report = entity.getData();
						report.setLastModifiedDate(new Date());
						entity.setData(report);
						LOGGER.info("Updating report with id: {}", entity.getId());
						try {
							em.merge(entity);
						} catch (Exception e) {
							LOGGER.error("Failed to update data for id {} at startup with exception {}", entity.getId(),
									e.getMessage());
						}
						LOGGER.info("Report updated with id: {}", entity.getId());		
					}
				}
			} catch (Exception e) {
				LOGGER.error("Failed to update data at startup with exception {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}		

	@Override
	public List<ReportNsql> getAllWithFiltersUsingNativeQuery(Boolean published, List<String> statuses, String userId,
			Boolean isAdmin, List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy,
			String sortOrder, String division, List<String> department, List<String> processOwner, List<String> art) {
		Query q = getNativeQueryWithFilters("", published, statuses, userId, isAdmin, searchTerms, tags, offset, limit,
				sortBy, sortOrder, "", "", division, department, processOwner, art);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<ReportNsql> convertedResults = results.stream().map(temp -> {
			ReportNsql entity = new ReportNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				Report tempReport = mapper.readValue(jsonData, Report.class);
				entity.setData(tempReport);
			} catch (Exception e) {
				LOGGER.error("Failed to fetch reports with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

	@Override
	public Long getCountUsingNativeQuery(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, String division, List<String> department,
			List<String> processOwner, List<String> art) {

		Query q = getNativeQueryWithFilters("select count(*) ", published, statuses, userId, isAdmin, searchTerms, tags,
				0, 0, "", "asc", "", "", division, department, processOwner, art);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	private Query getNativeQueryWithFilters(String selectFieldsString, Boolean published, List<String> statuses,
			String userId, Boolean isAdmin, List<String> searchTerms, List<String> tags, int offset, int limit,
			String sortBy, String sortOrder, String additionalPredicatesString, String groupByString, String division,
			List<String> department, List<String> processOwner, List<String> art) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from report_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(published, statuses, userId, isAdmin, searchTerms, tags,
				division, department, processOwner, art);
		String query = prefix + basicpredicate + consolidatedPredicates;
		String sortQueryString = "";
		if (StringUtils.hasText(sortBy)) {
			switch (sortBy) {
			case "productName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'productName')) ";
				break;
			case "status":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'description','status')) ";
				break;
			case "department":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'description','department')) ";
				break;
			case "art":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'description','agileReleaseTrain')) ";
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
		if (additionalPredicatesString != null && !"".equalsIgnoreCase(additionalPredicatesString))
			query = query + " " + additionalPredicatesString + " \n";
		if (groupByString != null && !"".equalsIgnoreCase(groupByString))
			query = query + " " + groupByString + " \n";
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
		Query q = em.createNativeQuery(query);
		return q;
	}
	@Override
	public Integer getCountBasedPublishReport(Boolean published) {
		Query q = getCountQueryWithFilters("select count(*) from ", published);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.intValue();
	}

	private Query getCountQueryWithFilters(String selectFieldsString, Boolean published) {
		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "report_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateBoolen(published);
		String query = prefix + basicpredicate + consolidatedPredicate;
		Query q = em.createNativeQuery(query);
		//LOGGER.info("sql query {} ",q);
		return q;
	}

	private String buildPredicateBoolen(Boolean published) {
		if (published != false) {
			return " and ((jsonb_extract_path_text(data,'publish')) in (" +"'"+ published +"'"+ "))";
		}
		return "";
	}

	private String buildPredicateString(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, String division, List<String> department,
			List<String> processOwner, List<String> art) {

		return getPublishedAndAccessPredicate(published, userId, isAdmin) + "\n"
				+ getProjectStatusesPredicateString(statuses) + "\n" + getSearchTermsPredicateString(searchTerms) + "\n"
				+ getTagsPredicateString(tags) + "\n" + getDivisionsPredicateString(division) + "\n"
				+ getDepartmentsPredicateString(department) + "\n" + getProcessOwnersPredicateString(processOwner)
				+ "\n" + getArtsPredicateString(art);
	}

	private String getPublishedAndAccessPredicate(Boolean published, String userId, Boolean isAdmin) {
		String allTrueCondition = " (jsonb_extract_path_text(data,'publish') in ('true')) ";
		String allFalseCondition = " (jsonb_extract_path_text(data,'publish') in ('false')) ";
		String hasAccessPredicate = null;
		String query = "";
		if (userId != null) {

			String isReportOwnerPredicate = " lower(jsonb_extract_path_text(data,'member','reportOwners')) like " + "'%"
					+ userId.toLowerCase() + "%'";

			String isReportAdminPredicate = " lower(jsonb_extract_path_text(data,'member','reportAdmins')) like " + "'%"
					+ userId.toLowerCase() + "%'";

			hasAccessPredicate = " (" + isReportOwnerPredicate + " or " + isReportAdminPredicate + ") ";

		}
		if (published != null) {
			if (published) {
				query += " and " + allTrueCondition;
			} else if (!published && isAdmin) {
				query += " and " + allFalseCondition;
			} else if (!published && !isAdmin) {
				query += " and " + allFalseCondition + " and " + hasAccessPredicate;
			}
		} else if (!isAdmin) {
			query += " and (" + allTrueCondition + " or " + hasAccessPredicate + ")";
		}

		return query;
	}

	private String getProjectStatusesPredicateString(List<String> statuses) {
		if (statuses != null && !statuses.isEmpty()) {
			String commaSeparatedstatuses = statuses.stream().collect(Collectors.joining("','", "'", "'"));
			return "  and ((jsonb_extract_path_text(data,'description','status') in (" + commaSeparatedstatuses
					+ "))) ";
		}
		return "";
	}

	private String getSearchTermsPredicateString(List<String> searchTerms) {
		if (searchTerms != null && !searchTerms.isEmpty()) {
			String delimiterSeparatedSearchTerms = searchTerms.stream()
					.map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase()).collect(Collectors.joining("%|%", "%", "%"));

			delimiterSeparatedSearchTerms = "'" + delimiterSeparatedSearchTerms + "'";
			return "  and (" + "lower(jsonb_extract_path_text(data,'productName')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','tags')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','status')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','integratedPortal')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','frontendTechnologies')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','agileReleaseTrain')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'customer','internalCustomers')) similar to "
					+ delimiterSeparatedSearchTerms + " or " + "lower(jsonb_extract_path_text(data,'kpis')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'singleDataSources')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'dataWarehouses')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','division')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'member','reportOwners')) similar to "
					+ delimiterSeparatedSearchTerms + " or "
					+ "lower(jsonb_extract_path_text(data,'description','department')) similar to "
					+ delimiterSeparatedSearchTerms + " ) ";
		}
		return "";
	}

	private String getTagsPredicateString(List<String> tags) {
		if (tags != null && !tags.isEmpty()) {
			String delimiterSeparatedTags = tags.stream().map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase())
					.collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedTags = "'" + delimiterSeparatedTags + "'";
			return "  and (lower(jsonb_extract_path_text(data,'description','tags')) similar to "
					+ delimiterSeparatedTags + " ) ";
		}
		return "";
	}

	private String getDepartmentsPredicateString(List<String> departments) {
		if (departments != null && !departments.isEmpty()) {
			String delimiterSeparatedDepartments = departments.stream().collect(Collectors.joining("','", "'", "'"));
			return "  and ((jsonb_extract_path_text(data,'description','department') in ("
					+ delimiterSeparatedDepartments + "))) ";
		}
		return "";
	}

	private String getArtsPredicateString(List<String> arts) {
		if (arts != null && !arts.isEmpty()) {
			String delimiterSeparatedArts = arts.stream().map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase())
					.collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedArts = "'" + delimiterSeparatedArts + "'";
			return "  and (lower(jsonb_extract_path_text(data,'description','agileReleaseTrain')) similar to "
					+ delimiterSeparatedArts + " ) ";
		}
		return "";
	}

	private String getProcessOwnersPredicateString(List<String> processOwners) {
		if (processOwners != null && !processOwners.isEmpty()) {
			String delimiterSeparatedProcessOwners = processOwners.stream().map(String::toLowerCase)
					.collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedProcessOwners = "'" + delimiterSeparatedProcessOwners + "'";
			return "  and (lower(jsonb_extract_path_text(data,'customer','internalCustomers')) similar to "
					+ delimiterSeparatedProcessOwners + " ) ";
		}
		return "";
	}

	@Override
	public List<TeamMemberVO> getAllProcessOwnerUsingNativeQuery() {
		String prefix = "select cast(data -> 'customer' -> 'internalCustomers' as text) from report_nsql";
		String basicpredicate = " where jsonb_extract_path_text(data,'customer','internalCustomers') is not null";
		String query = prefix + basicpredicate;
		return getReportOwners(query);
	}



	private List<TeamMemberVO> getReportOwners(String query) {
		Query q = em.createNativeQuery(query);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<Object> results = q.getResultList();
		List<InternalCustomerVO> convertedResults = new ArrayList<>();
		for (Object data : results) {
			try {
				String jsonData = data != null ? data.toString() : "";
				List<InternalCustomerVO> list = mapper.readValue(jsonData,
						new TypeReference<List<InternalCustomerVO>>() {
						});
				convertedResults.addAll(list);
			} catch (Exception e) {
				LOGGER.error("Failed to fetch report owners with exception {} ", e.getMessage());

			}
		}

		Map<String, TeamMemberVO> shortIdMap = new HashMap<>();
		for (InternalCustomerVO item : convertedResults) {
			if (item.getProcessOwner() != null && StringUtils.hasText(item.getProcessOwner().getShortId())) {
				shortIdMap.put(item.getProcessOwner().getShortId(), item.getProcessOwner());
			}
		}
		return shortIdMap.values().stream().collect(Collectors.toList());
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
					query += "(jsonb_extract_path_text(data,'description','division', 'id') in ('" + divList[0] + "')";
					String commaSeparatedinSubdivisions = Arrays.asList(divList[1].split(",")).stream()
							.collect(Collectors.joining("','", "'", "'"));
					if (isEmptySubdivision) {
						query += " and (jsonb_extract_path_text(data,'description', 'division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
						query += " or jsonb_extract_path_text(data, 'description', 'division','subdivision','id') is null)";
					} else {
						query += " and jsonb_extract_path_text(data, 'description','division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
					}
					query += ")";
				} else {
					query += "jsonb_extract_path_text(data,'description','division','id') in ('" + divList[0] + "')";
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
}
