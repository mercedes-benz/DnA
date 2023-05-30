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

package com.daimler.data.service.common;

import java.util.List;

import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.datatransfer.ChangeLogVO;

public interface CommonService<V, T, ID> {

	List<V> getAll();

	List<V> getAll(int limit, int offset);

	V getById(ID id);

    V updateByID(V vo);

	V getByUniqueliteral(String uniqueLiteral, String value);

	List<V> getAllSortedByUniqueLiteralAsc(String uniqueLiteral);

	List<V> getAllSortedByUniqueLiteralDesc(String uniqueLiteral);

	List<V> getAllSortedByUniqueLiteral(int limit, int offset, String uniqueLiteral,
			CommonDataRepositoryImpl.SORT_TYPE sortOrder);

	V create(V vo);

	void insertAll(List<V> voList);

	void deleteAll();

	boolean deleteById(ID id);

	Long getCount();

	String currentUserName(CreatedByVO currentUser);

	boolean verifyUserRoles();

	void notifyAllAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs);
}
