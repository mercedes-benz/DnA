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

package com.daimler.data.graphql;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.daimler.data.controller.ForecastController;
import com.daimler.data.dto.forecast.ForecastCollectionVO;
import com.daimler.data.dto.forecast.ForecastVO;
import com.daimler.data.service.forecast.ForecastService;

@Component
public class ChronosQuery implements GraphQLQueryResolver {

	@Autowired
	private ForecastService service;

	@Autowired
	private ForecastController forecastController;

	public Optional<ForecastVO> getForecastProject(String id) {
		return Optional.of(this.service.getById(id));
	}

	public ForecastCollectionVO getForecastProjects(int offset, int limit) {
		ResponseEntity<ForecastCollectionVO> forecastProjects = forecastController.getAll(offset, limit);
		if (forecastProjects != null && forecastProjects.getBody() != null) {
			return forecastProjects.getBody();
		} else {
			return null;
		}
	}


}
