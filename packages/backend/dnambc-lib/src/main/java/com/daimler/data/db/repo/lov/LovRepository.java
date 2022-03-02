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

import com.daimler.data.db.entities.lov.BenefitRelevanceNsql;
import com.daimler.data.db.entities.lov.BusinessGoalNsql;
import com.daimler.data.db.entities.lov.CategoryNsql;
import com.daimler.data.db.entities.lov.MaturityLevelNsql;
import com.daimler.data.db.entities.lov.StrategicRelevanceNsql;
import com.daimler.data.db.entities.lov.DataStrategyDomainNsql;

public interface LovRepository  {
	
	/**
	 * To fetch all business Goals. 
	 * 
	 * @return businessGoals
	 */
	List<BusinessGoalNsql> getAllBusinessGoal();
	
	/**
	 * To fetch all Benefit Relevances
	 * 
	 * @return benefitRelevances
	 */
	List<BenefitRelevanceNsql> getAllBenefitRelevance();
	
	/**
	 * To fetch all Strategic Relevances
	 * 
	 * @return strategicRelevances
	 */
	List<StrategicRelevanceNsql> getAllStrategicRelevance();
	
	/**
	 * To fetch all maturityLevels
	 * 
	 * @return maturityLevels
	 */
	List<MaturityLevelNsql> getAllMaturityLevel();
	
	/**
	 * To fetch all categories
	 * 
	 * @return categories
	 */
	List<CategoryNsql> getAllCategory();
	
	/**
	 * To fetch all strategyDomains
	 * 
	 * @return strategyDomains
	 */
	List<DataStrategyDomainNsql> getAllStrategyDomain();
}
