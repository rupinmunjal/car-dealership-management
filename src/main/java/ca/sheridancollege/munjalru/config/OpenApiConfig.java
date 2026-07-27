package ca.sheridancollege.munjalru.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI carDealershipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Car Dealership Management API")
                        .description("""
                                Multi-tenant car dealership management platform with role-based access control.
                                
                                ## Roles
                                - **SITE_ADMIN** — platform-wide administration (dealers, packages, analytics)
                                - **DEALER_ADMIN** — manages a single dealership (cars, employees, settings)
                                - **DEALER_EMPLOYEE** — scoped access granted by explicit permissions (e.g. CAN_ADD_CAR)
                                
                                ## Authentication
                                All endpoints except `/api/v1/auth/**` require a valid JWT.
                                Obtain a token via `POST /api/v1/auth/login`, then click **Authorize** and paste it as `Bearer <token>`.
                                
                                ## Error Responses
                                | Code | Meaning |
                                |------|---------|
                                | 400  | Validation or business-rule violation |
                                | 401  | Missing, expired, or invalid JWT |
                                | 403  | Insufficient role / permission / dealer-scope |
                                | 404  | Resource not found |
                                | 409  | Conflict (duplicate email, seat limit, downgrade) |
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Rupin Munjal")
                                .email("rupinmunjal@sheridancollege.ca"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("http://localhost:5000").description("Docker staging"),
                        new Server().url("http://localhost:6001").description("Docker production")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token from /api/v1/auth/login")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Registration and login (public)"),
                        new Tag().name("Cars").description("CRUD for car inventory — scoped by dealer and permissions"),
                        new Tag().name("Dealers").description("Dealer management, registration, status, settings, and package assignment"),
                        new Tag().name("Packages").description("Subscription package CRUD — SITE_ADMIN only"),
                        new Tag().name("Employees").description("Employee CRUD and permission management within a dealer")
                ));
    }
}
