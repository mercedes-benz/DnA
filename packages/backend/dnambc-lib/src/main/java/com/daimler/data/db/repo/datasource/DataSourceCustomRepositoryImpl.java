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

package com.daimler.data.db.repo.datasource;

import com.daimler.data.db.entities.DataSourceNsql;
import com.daimler.data.db.jsonb.DataSource;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class DataSourceCustomRepositoryImpl extends CommonDataRepositoryImpl<DataSourceNsql, String>
		implements DataSourceCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DataSourceCustomRepositoryImpl.class);
	private static final String DATASOURCE_NSQL = "datasource_nsql";
	@Override
	public List<DataSourceNsql> getAllDataCatalogs(String source,String sortOrder) {		
		Query q = getNativeQueryWithFilters("select cast(id as text), cast(data as text) from ", source,sortOrder);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();		
		List<DataSourceNsql> convertedResults = results.stream().map(temp -> {
			DataSourceNsql entity = new DataSourceNsql();
			try {
				String id = temp[0] != null ? temp[0].toString() : "";				
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				entity.setId(id);
				if (StringUtils.hasText(jsonData)) {
					DataSource dataSource = mapper.readValue(jsonData, DataSource.class);
					entity.setData(dataSource);
				}
			} catch (Exception e) {
				LOGGER.error("Failed while fetching data catalogs for solutions using native {} ", e.getMessage());
			}
			return entity;
		}).collect(Collectors.toList());	
		
		return convertedResults;
	
	}
	
	public Query getNativeQueryWithFilters(String selectFieldsString, String source, String sortOrder) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + DATASOURCE_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(source);
		String query = prefix + basicpredicate + consolidatedPredicate;		
		String sortQueryString = " order by lower(jsonb_extract_path_text(data,'name')) ";
		if ("desc".equalsIgnoreCase(sortOrder))
			sortQueryString = sortQueryString + " desc ";
		else
			sortQueryString = sortQueryString + " asc ";
		query = query + sortQueryString;
		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
	private String buildPredicateString(String source) {
		if (source != null && !source.isEmpty()) {			
			return " and (lower(jsonb_extract_path_text(data,'source')) = (" +"'"+ source.toLowerCase() +"'"+ "))";
		}
		return "";
	}

}
