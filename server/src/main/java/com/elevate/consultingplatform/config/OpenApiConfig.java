package com.elevate.consultingplatform.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Elevate Consulting Platform API", version = "0.1.0")
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clientApi() {
        return GroupedOpenApi.builder()
                .group("client")
                .pathsToMatch("/api/v1/client/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("chat")
                .pathsToMatch("/api/chat/**", "/api/v1/chat/**")
                .build();
    }

    @Bean
    public GroupedOpenApi questionnaireApi() {
        return GroupedOpenApi.builder()
                .group("questionnaire")
                .pathsToMatch("/api/v1/questionnaire/**")
                .build();
    }

    @Bean
    public GroupedOpenApi integrationsApi() {
        return GroupedOpenApi.builder()
                .group("integrations")
                .pathsToMatch("/api/v1/integrations/**")
                .build();
    }
}
