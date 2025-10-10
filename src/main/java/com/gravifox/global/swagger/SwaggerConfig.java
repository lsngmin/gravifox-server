package com.gravifox.global.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.info.Info;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GraviFox API Documentation",
                description = "Leverage the GraviFox API to seamlessly integrate systems and data, while expanding functionality. This document provides all the information necessary to use the API effectively.",
                version = "v1"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@RequiredArgsConstructor
public class SwaggerConfig {
//    @Bean
//    public OpenAPI openAPI() {
//        SecurityScheme securityScheme = getSecurityScheme();
//        SecurityRequirement securityRequirement = getSecurityRequireMent();
//        return new OpenAPI()
//                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
//                .security(List.of(securityRequirement));
//    }
//
//    private SecurityScheme getSecurityScheme() {
//        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
//                .in(SecurityScheme.In.HEADER).name("Authorization");
//    }
//
//    private SecurityRequirement getSecurityRequireMent() {
//        return new SecurityRequirement().addList("bearerAuth");
//    }


}
