package com.daimler.data.db.repo.common;

import java.util.List;

public interface CommonDataRepository<T, ID> {

	void deleteAll();

	void insertAll(List<T> tNsqlList);

	T findbyUniqueLiteral(String uniqueliteralName, String value);

	List<T> findAllSortyByUniqueLiteral(int limit, int offset, String uniqueLiteralName,
			CommonDataRepositoryImpl.SORT_TYPE sortOrder);

	List<T> findAllSortyByUniqueLiteral(String uniqueLiteralName, CommonDataRepositoryImpl.SORT_TYPE sortOrder);

	void update(T entity);

	List<T> findAll(int limit, int offset);

	Long getCount(int limit, int offset);

}

