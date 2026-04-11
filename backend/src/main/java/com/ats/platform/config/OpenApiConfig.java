package com.ats.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String BEARER = "bearer-jwt";

	@Bean
	public OpenAPI atsOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("ATS Platform API")
						.version("v1")
						.description("REST API for the ATS portfolio project. Use POST /api/auth/login to obtain a JWT, then Authorize with Bearer token."))
				.components(new Components().addSecuritySchemes(BEARER,
						new SecurityScheme()
								.name(BEARER)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
