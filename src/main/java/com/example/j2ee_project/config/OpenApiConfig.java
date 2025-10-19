package com.example.j2ee_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define servers (local and production, adjust as needed)
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server prodServer = new Server()
                .url("https://api.restaurant-management.com")
                .description("Production Server");

        // Define security scheme (JWT for auth, if applicable)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);

        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Management and Booking System API")
                        .version("1.0.0")
                        .description("API documentation for the Restaurant Management and Table Booking System, built with Spring Boot for J2EE Project.")
                        .contact(new Contact()
                                .name("Pham Thanh Su")
                                .email("thanhsu142.dev@gmail.com")
                                .url("https://github.com/LilsShine142"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(localServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }
}