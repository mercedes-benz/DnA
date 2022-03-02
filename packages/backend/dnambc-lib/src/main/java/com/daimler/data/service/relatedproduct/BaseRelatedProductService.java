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

package com.daimler.data.service.relatedproduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.RelatedproductAssembler;
import com.daimler.data.db.entities.RelatedProductNsql;
import com.daimler.data.db.repo.relatedproduct.RelatedProductCustomRepository;
import com.daimler.data.db.repo.relatedproduct.RelatedProductRepository;
import com.daimler.data.dto.relatedProduct.RelatedProductVO;
import com.daimler.data.service.common.BaseCommonService;
import com.daimler.data.service.solution.SolutionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseRelatedProductService extends BaseCommonService<RelatedProductVO, RelatedProductNsql, String>
		implements RelatedProductService {

	@Autowired
	private RelatedProductCustomRepository relatedProductCustomRepo;
	@Autowired
	private RelatedProductRepository jpaRepo;
	@Autowired
	private RelatedproductAssembler relatedProductAssembler;
	@Autowired
	private SolutionService solutionService;

	public BaseRelatedProductService() {
		super();
	}

	/**
	 * deleteRelatedProduct
	 * <P>
	 * delete related product by given Id
	 * 
	 * @param relatedProductIdToDelete
	 */
	@Transactional
	@Override
	public boolean deleteRelatedProduct(final String relatedProductIdToDelete) {
		RelatedProductNsql relatedProductEntity = jpaRepo.getOne(relatedProductIdToDelete);
		String relatedProductName = relatedProductEntity.getData().getName();
		log.debug("Calling solutionService deleteTagForEachSolution to delete cascading refences to relatedProduct {}",
				relatedProductIdToDelete);
		solutionService.deleteTagForEachSolution(null, relatedProductName, SolutionService.TAG_CATEGORY.RELATEDPRODUCT);
		return deleteById(relatedProductIdToDelete);
	}
}
