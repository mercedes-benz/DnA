package com.daimler.data.db.repo.workspace;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkspaceCustomAdditionalServiceRepoImpl extends CommonDataRepositoryImpl<CodeServerAdditionalServiceNsql, String>
        implements WorkspaceCustomAdditionalServiceRepo {

        @Override
        public List<CodeServerAdditionalServiceNsql> findAllServices(int offset, int limit) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<CodeServerAdditionalServiceNsql> cq = cb.createQuery(CodeServerAdditionalServiceNsql.class);
                Root<CodeServerAdditionalServiceNsql> root = cq.from(entityClass);
                CriteriaQuery<CodeServerAdditionalServiceNsql> getAll = cq.select(root);
                TypedQuery<CodeServerAdditionalServiceNsql> getAllQuery = em.createQuery(getAll);
                return getAllQuery.getResultList();
        }

        @Override
        public String findByServiceName(String serviceName) {
                List<Object[]> results = new ArrayList<>();
                String getQuery = "SELECT CAST(jsonb_extract_path_text(data, 'additionalProperties') AS text) AS additionalProperties "
                                + "FROM public.additional_service_nsql " +
                                 "WHERE jsonb_extract_path_text(data, 'serviceName') = '" + serviceName + "'";
                try {
                        Query query = em.createNativeQuery(getQuery);
                        results = query.getResultList();
                        return  results.toString();
                } catch (Exception e) {
                        log.error("Failed to query additional services while creating server.");
                }
                return null;
        }

        @Override
        public CodeServerAdditionalServiceNsql findByAddServiceName(String serviceName) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<CodeServerAdditionalServiceNsql> cq = cb.createQuery(CodeServerAdditionalServiceNsql.class);
                Root<CodeServerAdditionalServiceNsql> root = cq.from(entityClass);
                CriteriaQuery<CodeServerAdditionalServiceNsql> getAll = cq.select(root);
                Predicate con = cb.equal(cb.lower(
                                cb.function("jsonb_extract_path_text", String.class, root.get("data"),
                                                cb.literal("serviceName"))),
                                serviceName.toLowerCase());
                Predicate pMain = cb.and(con);
                cq.where(pMain);
                TypedQuery<CodeServerAdditionalServiceNsql> getAllQuery = em.createQuery(getAll);
                List<CodeServerAdditionalServiceNsql> entities = getAllQuery.getResultList();
                if (entities != null && entities.size() > 0)
                        return entities.get(0);
                else
                        return null;
        }

        @Override
        public CodeServerAdditionalServiceNsql findByAddServiceId(String id) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<CodeServerAdditionalServiceNsql> cq = cb.createQuery(CodeServerAdditionalServiceNsql.class);
                Root<CodeServerAdditionalServiceNsql> root = cq.from(entityClass);
                CriteriaQuery<CodeServerAdditionalServiceNsql> getAll = cq.select(root);
                Predicate con = cb.equal(root.get("id"), id);
                Predicate pMain = cb.and(con);
                cq.where(pMain);
                TypedQuery<CodeServerAdditionalServiceNsql> getAllQuery = em.createQuery(getAll);
                List<CodeServerAdditionalServiceNsql> entities = getAllQuery.getResultList();
                if (entities != null && entities.size() > 0)
                        return entities.get(0);
                else
                        return null;

        }

        @Override
        @Transactional
        public GenericMessage deleteAddService(CodeServerAdditionalServiceNsql addService) {
                try {
                        if (addService != null) {
                                // CodeServerAdditionalServiceNsql existingAddService =
                                // findByfindByAddServiceName(addService.getData().getServiceName());
                                CriteriaBuilder cb = em.getCriteriaBuilder();
                                CriteriaDelete<CodeServerAdditionalServiceNsql> cd = cb
                                                .createCriteriaDelete(CodeServerAdditionalServiceNsql.class);
                                Root<CodeServerAdditionalServiceNsql> root = cd.from(CodeServerAdditionalServiceNsql.class);
                                cd.where(cb.equal(root.get("id"), addService.getId()));
                                em.createQuery(cd).executeUpdate();
                                return new GenericMessage("Additional Services deleted successfully");
                        } else {
                                return new GenericMessage("Additional Services not found");
                        }
                } catch (Exception e) {
                        return new GenericMessage("Failed to delete additional services: " + e.getMessage());
                }
        }


}
