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

package com.daimler.data.db.repo.userinfo;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.jsonb.UserInfo;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class UserInfoCustomRepositoryImpl extends CommonDataRepositoryImpl<UserInfoNsql, String>
		implements UserInfoCustomRepository {

	private static Logger logger = LoggerFactory.getLogger(UserInfoCustomRepositoryImpl.class);
	private static final String USERINFO_NSQL = "userinfo_nsql";

	@Override
	public List<UserInfoNsql> getAllWithFilters(String searchTerm, int limit, int offset, String sortBy,
			String sortOrder) {
		Query q = getNativeQueryWithFilters("", searchTerm, offset, limit, sortBy, sortOrder, "");
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		return results.stream().map(temp -> {
			UserInfoNsql entity = new UserInfoNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				UserInfo userInfo = mapper.readValue(jsonData, UserInfo.class);
				entity.setData(userInfo);
			} catch (Exception e) {
				logger.error("Failed while fetching all users using native query{} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).toList();
	}

	@Override
	public Long getCount(String searchTerm) {
		Query q = getNativeQueryWithFilters("select count(*) ", searchTerm, 0, 0, "", "", "");
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}

	/*
	 * To build native query with given identifiers
	 * 
	 */
	private Query getNativeQueryWithFilters(String selectFieldsString, String searchTerm, int offset, int limit,
			String sortBy, String sortOrder, String additionalPredicatesString) {

		String prefix = selectFieldsString != null && !"".equalsIgnoreCase(selectFieldsString) ? selectFieldsString
				: "select cast(id as text), cast(data as text) ";
		prefix = prefix + "from " + USERINFO_NSQL;
		String basicpredicate = " where (id is not null)";
		String consolidatedPredicates = buildPredicateString(searchTerm);
		String query = prefix + basicpredicate + consolidatedPredicates;
		String sortQueryString = "";
		if (StringUtils.hasText(sortBy)) {
			switch (sortBy) {
			case "id":
				sortQueryString = " order by lower(id) ";
				break;
			case "firstName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'firstName')) ";
				break;
			case "lastName":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'lastName')) ";
				break;
			case "email":
				sortQueryString = " order by lower(jsonb_extract_path_text(data,'email')) ";
				break;
			case "roles":
				sortQueryString = " order by lower(jsonb_array_elements(data -> 'roles') ->> 'name') ";
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
		if (limit > 0)
			query = query + " limit " + limit;
		if (offset >= 0)
			query = query + " offset " + offset;
		return em.createNativeQuery(query);
	}

	/*
	 * To build predicate for query with given identifier
	 * 
	 */
	private String buildPredicateString(String searchTerm) {
		String predicate = "";
		if (StringUtils.hasText(searchTerm)) {
			String querySearchTerm = " (lower(id) like '%" + searchTerm.toLowerCase() + "%' or "
					+ "lower(jsonb_extract_path_text(data,'firstName')) like '%" + searchTerm.toLowerCase() + "%' or "
					+ "lower(jsonb_extract_path_text(data,'lastName')) like '%" + searchTerm.toLowerCase() + "%' or "
					+ "lower(jsonb_extract_path_text(data,'email')) like '%" + searchTerm.toLowerCase() + "%' or "
					+ "lower(jsonb_extract_path_text(data,'divisionAdmins')) like '%" + searchTerm.toLowerCase() + "%')";
			predicate = predicate + " and " + querySearchTerm;
		}
		return predicate;
	}

	@Override
	public Optional<UserInfoNsql> findById(String id) {
		Query q = em.createNativeQuery("SELECT * from "+USERINFO_NSQL+" WHERE lower(id)= ?",UserInfoNsql.class);
		q.setParameter(1, id.toLowerCase());
		UserInfoNsql user = null;
		try {
			user =(UserInfoNsql) q.getSingleResult();
		}catch (Exception e) {
			logger.error("Failed while fetching user information:{}",e.getMessage());
			return Optional.empty();
		}
		return Optional.of(user);
	}

	@Override
	public Integer getNumberOfUsers() {
		String query = "select count(*) from userinfo_nsql";
		Query q = em.createNativeQuery(query);
		BigInteger result = (BigInteger) q.getSingleResult();
		return result.intValue();
	}

}
