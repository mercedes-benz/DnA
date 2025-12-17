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
    @Transactional
    public boolean updateLeanGovernanceBySpaceId(String spaceId, JsonNode leanGovernance) {
        log.debug("Attempting upsert for leanGovernance for spaceId: {}", spaceId);

        try {
            // Convert JsonNode to JSON string for PostgreSQL
            String leanGovernanceJson = objectMapper.writeValueAsString(leanGovernance);
            log.debug("LeanGovernance JSON: {}", leanGovernanceJson);

            // 1. Check if a row with the given spaceId already exists
            String checkExistenceQuery = "SELECT COUNT(*) FROM uiliciousworkspace_nsql WHERE data->>'spaceId' = ?";
            Query checkQuery = em.createNativeQuery(checkExistenceQuery);
            checkQuery.setParameter(1, spaceId);
            long count = ((Number) checkQuery.getSingleResult()).longValue();

            int affectedRows = 0;

            if (count > 0) {
                // Row exists, perform update
                String updateQuery = "UPDATE uiliciousworkspace_nsql " +
                                    "SET data = jsonb_set(data, '{leanGovernance}', CAST(? AS jsonb), true) " +
                                    "WHERE data->>'spaceId' = ?";

                log.debug("Executing UPDATE SQL Query: {}", updateQuery);
                log.debug("Parameters - leanGovernanceJson: {}, spaceId: {}", leanGovernanceJson, spaceId);

                Query query = em.createNativeQuery(updateQuery);
                query.setParameter(1, leanGovernanceJson);
                query.setParameter(2, spaceId);

                affectedRows = query.executeUpdate();
                log.debug("Updated {} rows for spaceId: {}", affectedRows, spaceId);
            } else {
                // Row does not exist, perform insert with UUID for id column
                String id = java.util.UUID.randomUUID().toString();
                String insertQuery = "INSERT INTO uiliciousworkspace_nsql (id, data) VALUES (?, jsonb_build_object('spaceId', ?, 'leanGovernance', CAST(? AS jsonb)))";

                log.debug("Executing INSERT SQL Query: {}", insertQuery);
                log.debug("Parameters - id: {}, spaceId: {}, leanGovernanceJson: {}", id, spaceId, leanGovernanceJson);

                Query query = em.createNativeQuery(insertQuery);
                query.setParameter(1, id);
                query.setParameter(2, spaceId);
                query.setParameter(3, leanGovernanceJson);

                affectedRows = query.executeUpdate();
                log.debug("Inserted {} rows for spaceId: {}", affectedRows, spaceId);
            }

            // Flush to ensure immediate persistence
            em.flush();

            return affectedRows > 0;
        } catch (Exception e) {
            log.error("Failed to upsert leanGovernance for spaceId: {}, error: {}", spaceId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public JsonNode findLeanGovernanceBySpaceId(String spaceId) {
        log.info("Finding leanGovernance by spaceId: {}", spaceId);
        String getQuery = "SELECT CAST(data->'leanGovernance' AS text) FROM uiliciousworkspace_nsql " +
                "WHERE data->>'spaceId' = :spaceId";

        Query query = em.createNativeQuery(getQuery);
        query.setParameter("spaceId", spaceId);
        
        log.info("SQL Query: {}", getQuery);
        log.info("Query parameter - spaceId: {}", spaceId);
        
        try {
            List<?> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                Object result = results.get(0);
                if (result != null) {
                    String jsonString = result.toString();
                    log.info("LeanGovernance found for spaceId: {}, raw value: {}", spaceId, jsonString);
                    return objectMapper.readTree(jsonString);
                }
            }
            log.info("No leanGovernance found for spaceId: {}", spaceId);
            return null;
        } catch (Exception e) {
            log.info("Error finding leanGovernance for spaceId: {}, error: {}", spaceId, e.getMessage(), e);
            return null;
        }
    }

}
