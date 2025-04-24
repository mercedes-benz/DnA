
package com.daimler.data.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition
@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI baseOpenAPI() {
		return new OpenAPI().info(new Info().title("Dashboard API").version("1.0.0").description("Dashboard API"));
	}

	@Bean
	public OpenAPI openAPI() {
		final String securitySchemeName = "bearerAuth";
		return new OpenAPI()
				.addServersItem(new Server().url("/"))
				.addSecurityItem(new SecurityRequirement()
						.addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName, new SecurityScheme()
								.name(securitySchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));

	}

	// @Bean
	// public OpenApiCustomizer openApiCustomiser() {
	// return openApi -> openApi.getPaths().values().stream()
	// .flatMap(pathItem -> pathItem.readOperations().stream())
	// .forEach(operation -> operation.addParametersItem(new HeaderParameter()
	// .$ref("#/components/parameters/Authorizatio")));
	// }

	// @Bean
	// public OpenAPI openAPI() {
	// return new OpenAPI()
	// .addServersItem(new Server().url("/ws789/proxy/8081"));

	// }

	// @Bean
	// public OperationCustomizer customGlobalHeaders() {

	// return (Operation operation, HandlerMethod handlerMethod) -> {

	// Parameter authorization = new Parameter()
	// .in(ParameterIn.HEADER.toString())
	// .schema(new StringSchema())
	// .name("Authorization")
	// .description("Authorization header")
	// .required(true);

	// operation.addParametersItem(authorization);

	// return operation;
	// };
	// }
}
