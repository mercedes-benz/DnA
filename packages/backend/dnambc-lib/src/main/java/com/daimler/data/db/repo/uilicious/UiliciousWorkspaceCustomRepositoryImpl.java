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

package com.daimler.data.db.repo.uilicious;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.UiliciousWorkspaceNsql;
import com.daimler.data.db.jsonb.UiliciousWorkspace;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class UiliciousWorkspaceCustomRepositoryImpl extends CommonDataRepositoryImpl<UiliciousWorkspaceNsql, String>
        implements UiliciousWorkspaceCustomRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode findUiliciousWorkspacesByEmail(String email) {
        log.info("Finding UiliciousWorkspace by createdBy email: {}", email);
        String getQuery = "SELECT CAST(data AS text) FROM uiliciousworkspace_nsql " +
                "WHERE data->'createdBy'->>'email' = :email";

        Query query = em.createNativeQuery(getQuery);

        query.setParameter("email", email);
        log.info("SQL Query: {}", getQuery);
        log.info("Query parameter - email: {}", email);
        log.info("Query object: {}", query);
        try {
            Object result = query.getSingleResult();
            if (result != null) {
                log.info("UiliciousWorkspace found for email: {}", result.toString());
                return objectMapper.readTree(result.toString());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("No workspace found for email: {}", email);
            return null;
        }

    }

    @Override
    @Transactional
    public boolean updateAccountIdByEmail(String email, String accountId) {
        log.info("Updating accountId for email: {} with accountId: {}", email, accountId);
        String updateQuery = "UPDATE uiliciousworkspace_nsql " +
                "SET data = jsonb_set(data, '{accountId}', to_jsonb(CAST(? AS text))) " +
                "WHERE data->'createdBy'->>'email' = ?";

        log.info("Update SQL Query: {}", updateQuery);
        log.info("Parameters - accountId: {}, email: {}", accountId, email);

        try {
            Query query = em.createNativeQuery(updateQuery);
            query.setParameter(1, accountId);
            query.setParameter(2, email);

            int updatedRows = query.executeUpdate();
            log.info("Updated {} rows for email: {}", updatedRows, email);

            // Flush to ensure immediate persistence
            em.flush();

            return updatedRows > 0;
        } catch (Exception e) {
            log.error("Failed to update accountId for email: {}, error: {}", email, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}


