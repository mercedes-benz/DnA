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

package com.daimler.data.db.repo.catalogManagement;



import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.FabricCatalogMetadataNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class FabricCatalogManagementCustomRepositoryImpl extends CommonDataRepositoryImpl<FabricCatalogMetadataNsql, String>
		implements FabricCatalogManagementCustomRepository {
        
   @Override
    public Optional<FabricCatalogMetadataNsql> findByServiceName(String serviceName) {
        String sql = "SELECT * FROM fabric_catalog_metadata_nsql " +
                     "WHERE jsonb_extract_path_text(data,'metadata', 'serviceName') = :serviceName";

        try {
            FabricCatalogMetadataNsql result = (FabricCatalogMetadataNsql) em
                .createNativeQuery(sql, FabricCatalogMetadataNsql.class)
                .setParameter("serviceName", serviceName)
                .getSingleResult();

            if (result != null) {
                return Optional.of(result);
            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            if (e instanceof NoResultException) {
                log.debug("No result found for serviceName: {}", serviceName);
            } else {
                log.error("Error occurred while fetching FabricCatalogMetadataNsql by serviceName: {}", serviceName, e);
            }
            return Optional.empty();
        }
    }

}