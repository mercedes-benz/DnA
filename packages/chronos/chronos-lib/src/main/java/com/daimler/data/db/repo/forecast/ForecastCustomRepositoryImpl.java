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

package com.daimler.data.db.repo.forecast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.NoResultException;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.Forecast;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ForecastCustomRepositoryImpl extends CommonDataRepositoryImpl<ForecastNsql, String>
		implements ForecastCustomRepository {

	@Override
	public long getTotalCount(String userId) {
		String user = userId.toLowerCase();
		String getCountStmt = " select count(*) from forecast_nsql where  ((lower(jsonb_extract_path_text(data,'createdBy','id')) = '" + user +
										"') or (lower(jsonb_extract_path_text(data,'collaborators')) similar to '%"+ user + "%'))";
		Query q = em.createNativeQuery(getCountStmt);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}
	
	@Override
	public List<ForecastNsql> getAll(String userId, int offset, int limit){
		String user = userId.toLowerCase();
		String getAllStmt = " select cast(id as text), cast(data as text) from forecast_nsql where  ((lower(jsonb_extract_path_text(data,'createdBy','id')) = '" + user +
										"') or (lower(jsonb_extract_path_text(data,'collaborators')) similar to '%"+ user + "%'))";
		if (limit > 0)
			getAllStmt = getAllStmt + " limit " + limit;
		if (offset >= 0)
			getAllStmt = getAllStmt + " offset " + offset;
		Query q = em.createNativeQuery(getAllStmt);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<ForecastNsql> convertedResults = results.stream().map(temp -> {
			ForecastNsql entity = new ForecastNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				Forecast tempForecast = mapper.readValue(jsonData, Forecast.class);
				entity.setData(tempForecast);
			} catch (Exception e) {
				log.error("Failed while fetching all forecast projects using native query with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}
	
	
	@Override
	public long getTotalRunsCount(String id) {
		String getCountStmt = " select jsonb_array_length(data->'runs') from forecast_nsql where  id = '" + id + "' and (jsonb_extract_path_text(data,'isDelete') is null or  jsonb_extract_path_text(data,'isDelete') in ('true'))";;
		Query q = em.createNativeQuery(getCountStmt);
		try {
			Integer results = (Integer) q.getSingleResult();
			return results.intValue();
		}
		catch(NoResultException e) {
			log.error("No Runs present for given forecast ID: {}", id );
			return 0;
		} catch (Exception e) {
			log.error("Failed while fetching all runs for forecast project: {} using native query with exception {} ", e.getMessage());
			return 0;
		}
	}
	
	@Override
	public List<String> getAllForecastIds() {
		String query = "select cast(id as text) from forecast_nsql"; 
		Query q = em.createNativeQuery(query);		
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();		
		List<String> convertedResults = new ArrayList<>();
		if(results != null && !results.isEmpty()) {
			for(Object result : results) {                 
	            try {
					String jsonData = result.toString() != null ? result.toString() : "";										
					convertedResults.add(jsonData);
				}
				catch(Exception e) {
					log.error("Exception Occured: {}", e.getMessage());
				}
	        }	
		}			
		
		return convertedResults;
	}


}
