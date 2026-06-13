package com.redditx.feed.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI feedServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RedditX Feed Service API")
                        .version("v1")
                        .description("Event-driven home feed and community feed APIs.")
                        .contact(new Contact()
                                .name("RedditX")
                                .email("support@redditx.local")))
                .components(jwtComponents());
    }

    private Components jwtComponents() {
        return new Components()
                .addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}