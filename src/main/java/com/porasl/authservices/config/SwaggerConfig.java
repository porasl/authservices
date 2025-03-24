package com.porasl.authservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration("authSwaggerConfig")
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("authentication-api")
            .pathsToMatch("/auth/**")
            .build();
    }
}
