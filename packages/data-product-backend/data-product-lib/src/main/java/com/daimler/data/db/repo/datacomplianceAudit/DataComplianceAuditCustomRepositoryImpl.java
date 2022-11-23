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

package com.daimler.data.db.repo.datacomplianceAudit;

import com.daimler.data.db.entities.DataComplianceAuditNsql;
import com.daimler.data.db.jsonb.DataComplianceAudit;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DataComplianceAuditCustomRepositoryImpl extends CommonDataRepositoryImpl<DataComplianceAuditNsql, String>
		implements DataComplianceAuditCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DataComplianceAuditCustomRepositoryImpl.class);
	private static final String DATACOMPLIANCE_AUDIT_NSQL = "datacompliance_audit_nsql";
	@Override
	public Long getAuditCount(String entityId) {
		// TODO Auto-generated method stub

		if(entityId != null && !entityId.isEmpty()) {		
			Query q = getCountQueryWithFilters("select count(*) from ", entityId);
			if(q.getSingleResult()!=null) {
				BigInteger results = (BigInteger) q.getSingleResult();
				return  results.longValue();
			}
			return 0L;
						
		}
		else {
			Query q = em.createNativeQuery("SELECT count(*) from "+DATACOMPLIANCE_AUDIT_NSQL);			
			BigInteger result = (BigInteger) q.getSingleResult();
			return result != null ? result.longValue() : 0;
		}			
	
	}
	
	public Query getCountQueryWithFilters(String selectFieldsString, String entityId) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + DATACOMPLIANCE_AUDIT_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(entityId);
		String query = prefix + basicpredicate + consolidatedPredicate;		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
	private String buildPredicateString(String entityId) {
		if (entityId != null && !entityId.isEmpty()) {			
			return " and ((jsonb_extract_path_text(data,'entityId')) in (" +"'"+ entityId +"'"+ "))";
		}
		return "";
	}

	@Override
	public List<DataComplianceAuditNsql> getAuditsByEntityId(String entityId, int offset, int limit) {

		
		Query q = getNativeQueryWithFilters("select cast (data as text) from ", entityId,offset,limit);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();		
		List<DataComplianceAuditNsql> convertedResults = new ArrayList<>();
		if(results != null && !results.isEmpty()) {
			for(Object result : results) {                 
	            try {
					String jsonData = result.toString() != null ? result.toString() : "";				
					DataComplianceAuditNsql entity = new DataComplianceAuditNsql();
					DataComplianceAudit audit=mapper.readValue(jsonData, DataComplianceAudit.class);
					entity.setData(audit);
					convertedResults.add(entity);
				}
				catch(Exception e) {
					LOGGER.error("Exception Occured: {}", e.getMessage());
				}
	        }	
		}			
		
		return convertedResults;
	
	}
	
	public Query getNativeQueryWithFilters(String selectFieldsString, String entityId,int offset, int limit) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + DATACOMPLIANCE_AUDIT_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(entityId);
		String query = prefix + basicpredicate + consolidatedPredicate;
		
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
}
