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

package com.daimler.data.db.repo.datacompliance;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataComplianceNsql;
import com.daimler.data.db.jsonb.DataCompliance;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class DataComplianceCustomRepositoryImpl extends CommonDataRepositoryImpl<DataComplianceNsql, String>
		implements DataComplianceCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DataComplianceCustomRepositoryImpl.class);
	private static String REGEX = "[\\[+\\]+:{}^~?\\\\/()><=\"!]";

	@Override
	public List<DataComplianceNsql> getAllWithFiltersUsingNativeQuery(String entityId, String entityName, String entityCountry,
			List<String> localComplianceOfficer, List<String> localComplianceResponsible,
			List<String> localComplianceSpecialist, int offset, int limit,
			String sortBy, String sortOrder) {
		Query q = getNativeQueryWithFilters("", entityId, entityName, entityCountry, localComplianceOfficer,
				localComplianceResponsible, localComplianceSpecialist, offset, limit, sortBy,
				sortOrder);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<DataComplianceNsql> convertedResults = results.stream().map(temp -> {
			DataComplianceNsql entity = new DataComplianceNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				DataCompliance tempDataCompliance = mapper.readValue(jsonData, DataCompliance.class);
				entity.setData(tempDataCompliance);
			} catch (Exception e) {
				LOGGER.error("Failed to fetch data compliance network list with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

	@Override
	public Long getCountUsingNativeQuery(String entityId, String entityName, String entityCountry, List<String> localComplianceOfficer,
			List<String> localComplianceResponsible,
			List<String> localComplianceSpecialist) {

		Query q = getNativeQueryWithFilters("select count(*) ", entityId, entityName, entityCountry, localComplianceOfficer,
				localComplianceResponsible, localComplianceSpecialist, 0, 0, "", "asc");
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	private Query getNativeQueryWithFilters(String selectFieldsString, String entityId, String entityName, String entityCountry,
			List<String> localComplianceOfficer, List<String> localComplianceResponsible,
			List<String> localComplianceSpecialist, int offset, int limit,
			String sortBy, String sortOrder) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from datacompliance_nsql";
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(entityId, entityName, entityCountry, localComplianceOfficer,
				localComplianceResponsible, localComplianceSpecialist);
		String query = prefix + basicpredicate + consolidatedPredicates;
		String sortQueryString = "";
		if (StringUtils.hasText(sortBy)) {
			switch (sortBy) {
			case "entityId":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'entityId')) ";
				break;
			case "entityName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'entityName')) ";
				break;
			case "entityCountry":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'entityCountry')) ";
				break;		
			case "localComplianceOfficer":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'localComplianceOfficer')) ";
				break;
			case "localComplianceResponsible":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'localComplianceResponsible')) ";
				break;
			case "localComplianceSpecialist":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'localComplianceSpecialist')) ";
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

	private String buildPredicateString(String entityId, String entityName, String entityCountry, List<String> localComplianceOfficer,
			List<String> localComplianceResponsible, List<String> localComplianceSpecialist) {

		return getEntityIdPredicate(entityId) + "\n" + getEntityNamePredicate(entityName) + "\n" + getEntityCountryPredicate(entityCountry) + "\n"
				+ getLCOPredicate(localComplianceOfficer) + "\n" + getLCRPredicate(localComplianceResponsible) + "\n"
				+ "\n" + getLCSPredicate(localComplianceSpecialist);
	}

	private String getEntityIdPredicate(String entityId) {
		if (StringUtils.hasText(entityId)) {
			return "  and (lower(jsonb_extract_path_text(data,'entityId')) similar to " + "'%"
					+ entityId.replaceAll(REGEX, "\\\\$0").toLowerCase() + "%'" + " ) ";
		}
		return "";
	}

	private String getEntityNamePredicate(String entityName) {
		if (StringUtils.hasText(entityName)) {
			return "  and (lower(jsonb_extract_path_text(data,'entityName')) similar to " + "'%"
					+ entityName.replaceAll(REGEX, "\\\\$0").toLowerCase() + "%'" + " ) ";
		}
		return "";
	}

	private String getEntityCountryPredicate(String entityCountry) {
		if (StringUtils.hasText(entityCountry)) {
			return "  and (lower(jsonb_extract_path_text(data,'entityCountry')) similar to " + "'%"
					+ entityCountry.replaceAll(REGEX, "\\\\$0").toLowerCase() + "%'" + " ) ";
		}
		return "";
	}

	private String getLCOPredicate(List<String> localComplianceOfficer) {
		if (localComplianceOfficer != null && !localComplianceOfficer.isEmpty()) {
			String delimiterSeparatedValue = localComplianceOfficer.stream()
					.map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase()).collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedValue = "'" + delimiterSeparatedValue + "'";
			return "  and (lower(jsonb_extract_path_text(data,'localComplianceOfficer')) similar to "
					+ delimiterSeparatedValue + " ) ";
		}
		return "";
	}

	private String getLCRPredicate(List<String> localComplianceResponsible) {
		if (localComplianceResponsible != null && !localComplianceResponsible.isEmpty()) {
			String delimiterSeparatedValue = localComplianceResponsible.stream()
					.map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase()).collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedValue = "'" + delimiterSeparatedValue + "'";
			return "  and (lower(jsonb_extract_path_text(data,'localComplianceResponsible')) similar to "
					+ delimiterSeparatedValue + " ) ";
		}
		return "";
	}

	private String getLCSPredicate(List<String> localComplianceSpecialist) {
		if (localComplianceSpecialist != null && !localComplianceSpecialist.isEmpty()) {
			String delimiterSeparatedValue = localComplianceSpecialist.stream()
					.map(n -> n.replaceAll(REGEX, "\\\\$0").toLowerCase()).collect(Collectors.joining("%|%", "%", "%"));
			delimiterSeparatedValue = "'" + delimiterSeparatedValue + "'";
			return "  and (lower(jsonb_extract_path_text(data,'localComplianceSpecialist')) similar to "
					+ delimiterSeparatedValue + " ) ";
		}
		return "";
	}

}