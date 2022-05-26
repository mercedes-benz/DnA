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

package com.daimler.data.service.lov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.daimler.data.assembler.LovAssembler;
import com.daimler.data.db.repo.lov.LovRepository;
import com.daimler.data.dto.lov.ClassificationVO;

@Service
public class BaseLovService implements LovService {

	private static Logger logger = LoggerFactory.getLogger(BaseLovService.class);

	@Autowired
	private LovRepository lovRepository;

	@Autowired
	private LovAssembler lovAssembler;

	public BaseLovService() {
		super();
	}

	@Override
	@Transactional
	public List<ClassificationVO> getAllClassificationType(String orderBy) {
		logger.info("Fetching all classification types.");
		// Fetching data as List of ClassificationNsql and converting to List of
		// ClassificationVO
		List<ClassificationVO> classificationsVO = new ArrayList<>(
				lovAssembler.toClassificationVOList(lovRepository.getAllClassificationType()));
		if (StringUtils.hasText(orderBy) && "desc".equals(orderBy)) {
			logger.info("Sorting values in descending order.");
			Collections.sort(classificationsVO, (o1, o2) -> (o2.getName().compareTo(o1.getName())));
		} else {
			logger.info("Sorting values in ascending order.");
			Collections.sort(classificationsVO, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		}
		return classificationsVO;
	}

}
