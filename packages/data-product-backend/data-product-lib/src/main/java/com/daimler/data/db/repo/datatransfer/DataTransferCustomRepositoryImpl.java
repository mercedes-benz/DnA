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

package com.daimler.data.db.repo.datatransfer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataTransferNsql;
import com.daimler.data.db.jsonb.datatransfer.DataTranfer;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class DataTransferCustomRepositoryImpl extends CommonDataRepositoryImpl<DataTransferNsql, String>
		implements DataTransferCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DataTransferCustomRepositoryImpl.class);
	private static String REGEX = "[\\[+\\]+:{}^~?\\\\/()><=\"!]";

	@Override
	public List<DataTransferNsql> getAllWithFiltersUsingNativeQuery(Boolean published, int offset, int limit,
			String sortBy, String sortOrder, String recordStatus, String datatransferIds, String userId, String providerUserId) {
		Query q = getNativeQueryWithFilters("", published, offset, limit, sortBy, sortOrder, recordStatus, datatransferIds, userId, providerUserId);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<DataTransferNsql> convertedResults = results.stream().map(temp -> {
			DataTransferNsql entity = new DataTransferNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				DataTranfer tempDataTransfer = mapper.readValue(jsonData, DataTranfer.class);
				entity.setData(tempDataTransfer);
			} catch (Exception e) {
				LOGGER.error("Failed to fetch datatransfers with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

	@Override
	public Long getCountUsingNativeQuery(Boolean published, String recordStatus, String datatransferIds, String userId, String providerUserId ) {

		Query q = getNativeQueryWithFilters("select count(*) ", published, 0, 0, "", "asc", recordStatus, datatransferIds, userId, providerUserId);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	private Query getNativeQueryWithFilters(String selectFieldsString, Boolean published, int offset, int limit,
			String sortBy, String sortOrder, String recordStatus, String datatransferIds, String userId, String providerUserId) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from datatransfer_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(published, recordStatus);
		String query = prefix + basicpredicate + consolidatedPredicates;
		if (datatransferIds != null && datatransferIds.length() > 0) {
			String dataTransferId = " and (jsonb_extract_path_text(data,'dataTransferId') in (" + datatransferIds + ")) ";
			query += dataTransferId;
		}
		if (userId != null) {
				String creator = " and (jsonb_extract_path_text(data, 'consumerInformation', 'createdBy', 'id') in ('" + userId + "')) ";
				query += creator;
		}
		if (providerUserId != null) {
			String creator = " and (jsonb_extract_path_text(data,'providerInformation','createdBy','id') in ('" + providerUserId + "')) ";
			query += creator;
		}
		String sortQueryString = "";
		if (StringUtils.hasText(sortBy)) {
			switch (sortBy) {
			case "dataTransferName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'dataTransferName')) ";
				break;
			case "dataTransferId":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'dataTransferId')) ";
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

	private String buildPredicateString(Boolean published, String recordStatus) {
		return getPublishedAndAccessPredicate(published) + "\n" + getRecordStatusPredicateString(recordStatus);
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
					query += "(jsonb_extract_path_text(data, 'providerInformation', 'contactInformation', 'division', 'id') in ('"
							+ divList[0] + "')";
					String commaSeparatedinSubdivisions = Arrays.asList(divList[1].split(",")).stream()
							.collect(Collectors.joining("','", "'", "'"));
					if (isEmptySubdivision) {
						query += " and (jsonb_extract_path_text(data,'providerInformation','contactInformation', 'division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
						query += " or jsonb_extract_path_text(data, 'providerInformation', 'contactInformation','division','subdivision','id') is null)";
					} else {
						query += " and jsonb_extract_path_text(data, 'providerInformation','contactInformation','division','subdivision','id') in ("
								+ commaSeparatedinSubdivisions + ")";
					}
					query += ")";
				} else {
					query += "jsonb_extract_path_text(data,'providerInformation','contactInformation','division','id') in ('"
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
	
	public Query getNativeQueryWithFilters(String selectFieldsString, String uniqueTransferName,String status) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) from";
		prefix = prefix + " datatransfer_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(uniqueTransferName,status);
		String query = prefix + basicpredicate + consolidatedPredicate;		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
	private String buildPredicateString(String uniqueTransferName, String status) {;
		if ((uniqueTransferName != null && !uniqueTransferName.isEmpty()) && (!status.isEmpty())) {			
			return " and ((jsonb_extract_path_text(data,'dataTransferName')) in (" +"'"+ uniqueTransferName +"'"+ "))" +  
					" and ((jsonb_extract_path_text(data,'recordStatus')) in (" +"'"+ status +"'"+ "))";
		}
		return "";
	}

	@Override
	public List<DataTransferNsql> getExistingDataTransfer(String uniqueTransferName, String status) {

		Query q = getNativeQueryWithFilters("select cast (data as text) from ", uniqueTransferName,status);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();				
		DataTransferNsql entity = new DataTransferNsql();
		List<DataTransferNsql> dataTransferNsqls = new ArrayList<>();
		if(results != null && !results.isEmpty()) {
			for(Object result : results) {
				try {
					String jsonData = result.toString() != null ? result.toString() : "";					
					DataTranfer dataTransfer = mapper.readValue(jsonData, DataTranfer.class);
					entity.setData(dataTransfer);
					dataTransferNsqls.add(entity);
				}
				catch(Exception e) {
					LOGGER.error("Exception Occured: {}", e.getMessage());
				}
			}
		}
		return dataTransferNsqls;				
	}
}