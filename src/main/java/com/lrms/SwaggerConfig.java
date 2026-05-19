package com.lrms;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("LRMS - Lodgings & Restaurant Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tanishq Saini")
                                .email("tanishq@dazzelr.tech")));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public GroupedOpenApi fullApi() {
        return GroupedOpenApi.builder()
                .group("0-Full API")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi lodgingApi() {
        return GroupedOpenApi.builder()
                .group("1-Lodging")
                .pathsToMatch("/api/rooms/**", "/api/bookings/**", "/api/guests/**")
                .build();
    }

    @Bean
    public GroupedOpenApi restaurantApi() {
        return GroupedOpenApi.builder()
                .group("2-Restaurant")
                .pathsToMatch("/api/menu/**", "/api/tables/**", "/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi billingApi() {
        return GroupedOpenApi.builder()
                .group("3-Billing")
                .pathsToMatch("/api/bills/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("4-Administration")
                .pathsToMatch("/api/admin/**", "/api/auth/**")
                .build();
    }
}
