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

package com.daimler.data.db.repo.divisionAudit;

import com.daimler.data.db.entities.DivisionAuditNsql;
import com.daimler.data.db.jsonb.DivisionAudit;
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
public class DivisionAuditCustomRepositoryImpl extends CommonDataRepositoryImpl<DivisionAuditNsql, String>
		implements DivisionAuditCustomRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(DivisionAuditCustomRepositoryImpl.class);
	private static final String DIVISIONAUDIT_NSQL = "division_audit_nsql";
	@Override
	public List<DivisionAuditNsql> getAuditsByDivisionId( String divisionId,int offset, int limit) {
		
		Query q = getNativeQueryWithFilters("select cast (data as text) from ", divisionId,offset,limit);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();		
		List<DivisionAuditNsql> convertedResults = new ArrayList<>();
		if(results != null && !results.isEmpty()) {
			for(Object result : results) {                 
	            try {
					String jsonData = result.toString() != null ? result.toString() : "";				
					DivisionAuditNsql entity = new DivisionAuditNsql();
					DivisionAudit audit=mapper.readValue(jsonData, DivisionAudit.class);
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
	
	
	@Override
	public Long getAuditCount(String divisionId) {
		// TODO Auto-generated method stub
		if(divisionId != null && !divisionId.isEmpty()) {		
			Query q = getCountQueryWithFilters("select count(*) from ", divisionId);
			if(q.getSingleResult()!=null) {
				BigInteger results = (BigInteger) q.getSingleResult();
				return  results.longValue();
			}
			return 0L;
			
			
		}
		else {
			Query q = em.createNativeQuery("SELECT count(*) from "+DIVISIONAUDIT_NSQL);			
			BigInteger result = (BigInteger) q.getSingleResult();
			return result != null ? result.longValue() : 0;
		}			
	}
	
	public Query getNativeQueryWithFilters(String selectFieldsString, String divisionId,int offset, int limit) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + DIVISIONAUDIT_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(divisionId);
		String query = prefix + basicpredicate + consolidatedPredicate;
		
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
		
		Query q = em.createNativeQuery(query);
		return q;
	}
	
	public Query getCountQueryWithFilters(String selectFieldsString, String divisionId) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + DIVISIONAUDIT_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicate = buildPredicateString(divisionId);
		String query = prefix + basicpredicate + consolidatedPredicate;		
		Query q = em.createNativeQuery(query);
		return q;
	}


	private String buildPredicateString(String divisionId) {
		if (divisionId != null && !divisionId.isEmpty()) {			
			return " and ((jsonb_extract_path_text(data,'divisionId')) in (" +"'"+ divisionId +"'"+ "))";
		}
		return "";
	}
	

}
