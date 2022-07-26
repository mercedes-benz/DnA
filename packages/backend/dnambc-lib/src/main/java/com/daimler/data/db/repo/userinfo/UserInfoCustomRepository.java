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

import java.util.List;
import java.util.Optional;

import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;

public interface UserInfoCustomRepository extends CommonDataRepository<UserInfoNsql, String> {

	/**
	 * To return get all user information with given identifier
	 * 
	 * @param searchTerm
	 * @param limit
	 * @param offset
	 * @param sortBy
	 * @param sortOrder
	 * @return list of user info {@code List<UserInfoNsql>}
	 */
	public List<UserInfoNsql> getAllWithFilters(String searchTerm, int limit, int offset, String sortBy, String sortOrder);

	/**
	 * To return count based on given identifier
	 * 
	 * @param searchTerm
	 * @return total count {@code Long}
	 */
	public Long getCount(String searchTerm);
	
	/**
	 * Retrieves an entity by its id.
	 *
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 */
	Optional<UserInfoNsql> findById(String id);
}
