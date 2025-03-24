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

package com.daimler.data.db.repo.promptCraftSubscriptions;

import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.PromptCraftSubscriptionsNsql;
import com.daimler.data.db.jsonb.PromptCraftSubscriptions;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PromptCraftSubscriptionsCustomRepositoryImpl
                extends CommonDataRepositoryImpl<PromptCraftSubscriptionsNsql, String>
                implements PromptCraftSubscriptionsCustomRepository {

        @Override
        public List<PromptCraftSubscriptionsNsql> findAlluserSubscriptions(int limit, int offset, String userId) {
                String getQuery = "SELECT * FROM promptcraftsubscriptions_nsql " +
                                "WHERE EXISTS (" +
                                "  SELECT 1 FROM jsonb_array_elements(data->'projectMembers') AS member " +
                                "  WHERE member->>'id' = "+"'"+userId +"'"+
                                ") ";

                if (limit > 0)
                        getQuery = getQuery + " limit " + limit;
                if (offset >= 0)
                        getQuery = getQuery + " offset " + offset;

                Query query = em.createNativeQuery(getQuery, PromptCraftSubscriptionsNsql.class);
                return query.getResultList();
        }
}
