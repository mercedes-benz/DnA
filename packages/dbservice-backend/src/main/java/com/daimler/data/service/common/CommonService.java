package com.daimler.data.service.common;

import java.util.List;

public interface CommonService<V, T, ID> {

	List<V> getAll();

	List<V> getAll(int limit, int offset);

	V getById(ID id);

	V getByUniqueliteral(String uniqueLiteral, String value);

	List<V> getAllSortedByUniqueLiteralAsc(String uniqueLiteral);

	List<V> getAllSortedByUniqueLiteralDesc(String uniqueLiteral);

	List<V> getAllSortedByUniqueLiteral(int limit, int offset, String uniqueLiteral,
			CommonDataRepositoryImpl.SORT_TYPE sortOrder);

	V create(V vo);

	void insertAll(List<V> voList);

	void deleteAll();

	boolean deleteById(ID id);

	Long getCount(int limit, int offset);

	String currentUserName(CreatedByVO currentUser);

}