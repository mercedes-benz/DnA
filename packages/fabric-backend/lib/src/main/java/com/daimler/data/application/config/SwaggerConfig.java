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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.headers.authorization.token}")
    private String defaultAuthToken;

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("DnA-Fabric-Workspace management REST API documentation.")
                        .description("REST API uri document management. Description of all the available APIs along with request and response formats. Also provides "
                                + " options to try calling to execute running APIs and check")
                        .license(new License()
                                .name("Daimler AG 2020")
                                .url("https://github.com/Daimler/DnA/blob/master/LICENSE"))
                        .version("1.0.0"));
    }

    @Bean
    public OperationCustomizer globalHeaders() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("Content-Type")
                    .description("Content-Type")
                    .required(true)
                    .schema(new StringSchema()._default("application/json")));
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("Authorization")
                    .description("Authorization header")
                    .required(false)
                    .schema(new StringSchema()._default(defaultAuthToken)));
            return operation;
        };
    }
}
