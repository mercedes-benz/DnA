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

package com.daimler.data.db.repo.roles;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.AuthoriserRolesNsql;
import com.daimler.data.db.json.FabricWorkspace;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.daimler.data.dto.fabricWorkspace.DnaRoleCollectionVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AuthoriserRolesCustomRepositoryImpl extends CommonDataRepositoryImpl<AuthoriserRolesNsql, String>
		implements AuthoriserRolesCustomRepository {

	@Override
    public List<AuthoriserRolesNsql> getAll(String userId) {
        try {
            
            String jsonPath = "$.ownerDetails[*] ? (@.id == \"" + userId + "\")";
            String query = "SELECT * FROM user_created_roles_nsql WHERE jsonb_path_exists(data, cast(:jsonPath AS jsonpath))";
    
            return em.createNativeQuery(query, AuthoriserRolesNsql.class)
                        .setParameter("jsonPath", jsonPath)
                        .getResultList();
    
        } catch (Exception e) {
            log.error("Error querying roles for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
