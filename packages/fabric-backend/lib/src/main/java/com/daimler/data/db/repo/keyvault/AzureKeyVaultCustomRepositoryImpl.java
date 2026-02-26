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
package com.daimler.data.db.repo.keyvault;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.AzureKeyVaultNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AzureKeyVaultCustomRepositoryImpl extends CommonDataRepositoryImpl<AzureKeyVaultNsql, String>
        implements AzureKeyVaultCustomRepository {

    @Override
    public List<AzureKeyVaultNsql> findAllByCreator(String creatorId, int limit, int offset) {
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AzureKeyVaultNsql> cq = cb.createQuery(AzureKeyVaultNsql.class);
        Root<AzureKeyVaultNsql> root = cq.from(AzureKeyVaultNsql.class);

        Expression<String> createdByIdPath = cb.function(
            "jsonb_extract_path_text",
            String.class,
            root.get("data"),
            cb.literal("createdBy"),
            cb.literal("id")
        );

        Predicate creatorPredicate = cb.equal(
            cb.lower(createdByIdPath),
            cb.lower(cb.literal(creatorId))
        );
        
        cq.where(creatorPredicate);
        
        Expression<String> createdOnPath = cb.function(
            "jsonb_extract_path_text",
            String.class,
            root.get("data"),
            cb.literal("createdOn")
        );
        cq.orderBy(cb.desc(createdOnPath));
        
        TypedQuery<AzureKeyVaultNsql> query = em.createQuery(cq);
        
        if (offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }
}