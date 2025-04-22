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
 import java.util.Collection;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
 import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
 import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
 import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
 import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
 import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
 import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
 import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
 import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
 import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
 import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
 import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.env.Environment;
 import org.springframework.util.StringUtils;
 
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
 
     @Value("${swagger.headers.authorization.token}")
     private String defaultAuthToken;
 
     @Value("${swagger.headers.authorization.appId}")
     private String defaultAuthAppKey;
 
     @Value("${swagger.headers.authorization.apiKey}")
     private String defaultAuthAppId;
 
     @Bean
     public Docket api() {
         RequestParameter contentTypeParamBuilder = new RequestParameterBuilder().name("Content-Type")
                 .description("Content-Type").query(q -> q.defaultValue("application/json"))
                 .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).in(ParameterType.HEADER).required(true)
                 .build();
 
         RequestParameter authParamBuilder = new RequestParameterBuilder().name("Authorization")
                 .description("Authorization header").query(q -> q.defaultValue(defaultAuthToken))
                 .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).in(ParameterType.HEADER).required(false)
                 .build();
 
         // RequestParameter appId = new RequestParameterBuilder().name("appId")
         // 		.description("Authorization header").query(q -> q.defaultValue(defaultAuthAppId))
         // 		.query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).in(ParameterType.HEADER).required(false)
         // 		.build();
 
         // RequestParameter apiKey = new RequestParameterBuilder().name("apiKey")
         // 		.description("Authorization header").query(q -> q.defaultValue(defaultAuthAppKey))
         // 		.query(q -> q.model(m -> m.scalarModel(ScalarType.STRING))).in(ParameterType.HEADER).required(false)
         // 		.build();
 
         List<RequestParameter> params = new ArrayList<>();
         params.add(contentTypeParamBuilder);
         params.add(authParamBuilder);
         //params.add(appId);
         // params.add(apiKey);
 
         return new Docket(DocumentationType.SWAGGER_2).select()
                 .apis(RequestHandlerSelectors.basePackage("com.daimler.data.controller")).paths(PathSelectors.any())
                 .build().pathMapping("/").apiInfo(apiEndPointsInfo()).globalRequestParameters(params);
     }
 
     private ApiInfo apiEndPointsInfo() {
         return new ApiInfoBuilder().title("Chronos-Forecasting REST API Documentation.").description(
                 "REST API uri document management. Description of all the available APIs along with request and response formats. Also provides "
                         + " options to try calling to execute running APIs and check")
                 .license("Daimler AG 2020").licenseUrl("https://github.com/Daimler/DnA/blob/master/LICENSE")
                 .version("1.0.0").build();
     }
 
     @Bean
     public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
             ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
             EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
             WebEndpointProperties webEndpointProperties, Environment environment) {
         List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
         Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
         allEndpoints.addAll(webEndpoints);
         allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
         allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
         String basePath = webEndpointProperties.getBasePath();
         EndpointMapping endpointMapping = new EndpointMapping(basePath);
         boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
                 basePath);
         return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
                 corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath),
                 shouldRegisterLinksMapping, null);
     }
 
     private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
             String basePath) {
         return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
                 || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
     }
 
 }
 