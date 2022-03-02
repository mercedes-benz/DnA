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

package com.daimler.data.db.repo.lov;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.lov.BenefitRelevanceNsql;
import com.daimler.data.db.entities.lov.BusinessGoalNsql;
import com.daimler.data.db.entities.lov.CategoryNsql;
import com.daimler.data.db.entities.lov.MaturityLevelNsql;
import com.daimler.data.db.entities.lov.StrategicRelevanceNsql;
import com.daimler.data.db.entities.lov.DataStrategyDomainNsql;

@Repository
public class LovRepositoryImpl implements LovRepository {

	@PersistenceContext
	protected EntityManager em;

	/**
	 * getAllBusinessGoal
	 * <P>
	 * Fetch all business Goal from Database
	 * 
	 * @return List<BusinessGoalNsql>
	 */
	@Override
	public List<BusinessGoalNsql> getAllBusinessGoal() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BusinessGoalNsql> cq = cb.createQuery(BusinessGoalNsql.class);
		Root<BusinessGoalNsql> root = cq.from(BusinessGoalNsql.class);
		CriteriaQuery<BusinessGoalNsql> getAll = cq.select(root);
		TypedQuery<BusinessGoalNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

	/**
	 * getAllBenefitRelevance
	 * <P>
	 * Fetch all Benefit relevances from Database
	 * 
	 * @return List<BenefitRelevanceNsql>
	 */
	@Override
	public List<BenefitRelevanceNsql> getAllBenefitRelevance() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BenefitRelevanceNsql> cq = cb.createQuery(BenefitRelevanceNsql.class);
		Root<BenefitRelevanceNsql> root = cq.from(BenefitRelevanceNsql.class);
		CriteriaQuery<BenefitRelevanceNsql> getAll = cq.select(root);
		TypedQuery<BenefitRelevanceNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

	/**
	 * getAllStrategicRelevance
	 * <P>
	 * Fetch all Strategic relevances from Database
	 * 
	 * @return List<StrategicRelevanceNsql>
	 */
	@Override
	public List<StrategicRelevanceNsql> getAllStrategicRelevance() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<StrategicRelevanceNsql> cq = cb.createQuery(StrategicRelevanceNsql.class);
		Root<StrategicRelevanceNsql> root = cq.from(StrategicRelevanceNsql.class);
		CriteriaQuery<StrategicRelevanceNsql> getAll = cq.select(root);
		TypedQuery<StrategicRelevanceNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

	/**
	 * getAllMaturityLevel
	 * <P>
	 * Fetch all Maturity Level from Database
	 * 
	 * @return List<MaturityLevelNsql>
	 */
	@Override
	public List<MaturityLevelNsql> getAllMaturityLevel() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<MaturityLevelNsql> cq = cb.createQuery(MaturityLevelNsql.class);
		Root<MaturityLevelNsql> root = cq.from(MaturityLevelNsql.class);
		CriteriaQuery<MaturityLevelNsql> getAll = cq.select(root);
		TypedQuery<MaturityLevelNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

	/**
	 * getAllCategory
	 * <P>
	 * Fetch all Category from Database
	 * 
	 * @return List<CategoryNsql>
	 */
	@Override
	public List<CategoryNsql> getAllCategory() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CategoryNsql> cq = cb.createQuery(CategoryNsql.class);
		Root<CategoryNsql> root = cq.from(CategoryNsql.class);
		CriteriaQuery<CategoryNsql> getAll = cq.select(root);
		TypedQuery<CategoryNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

	@Override
	public List<DataStrategyDomainNsql> getAllStrategyDomain() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<DataStrategyDomainNsql> cq = cb.createQuery(DataStrategyDomainNsql.class);
		Root<DataStrategyDomainNsql> root = cq.from(DataStrategyDomainNsql.class);
		CriteriaQuery<DataStrategyDomainNsql> getAll = cq.select(root);
		TypedQuery<DataStrategyDomainNsql> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getResultList();
	}

}
