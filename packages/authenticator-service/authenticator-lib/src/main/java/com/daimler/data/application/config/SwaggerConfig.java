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

package com.daimler.data.application.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket apiWeb() {

		RequestParameter contentTypeParamBuilder = new RequestParameterBuilder().name("Content-Type")
				.description("content type").query(q -> q.defaultValue("application/json"))
				.query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).in(ParameterType.HEADER).required(true)
				.build();
		List<RequestParameter> globalRequestHeaderParms = new ArrayList<>();
		globalRequestHeaderParms.add(contentTypeParamBuilder);

		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.daimler.data.controller")).paths(PathSelectors.any())
				.build().pathMapping("/").apiInfo(apiEndPointsInfo()).globalRequestParameters(globalRequestHeaderParms);
		
	}

	
	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder().title("DNA AUTH REST API Documentation").description(
				"REST API uri document management. Description of all the available APIs along with request and response formats. Also provides "
						+ " options to try calling to execute running APIs and check")
				.license("MIT License, Copyright (c) 2019 Daimler TSS GmbH")
				.licenseUrl("https://github.com/Daimler/DnA/blob/master/LICENSE").version("1.0.0").build();
	}

}
