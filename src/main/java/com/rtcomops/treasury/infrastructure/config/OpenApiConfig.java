package com.rtcomops.treasury.infrastructure.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Treasury API Documentation (WebFlux).
 *
 * <p>This configuration sets up Swagger UI for interactive API documentation.
 * Access the documentation at: http://localhost:8080/api/swagger-ui.html</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI documentation with metadata about the reactive API.
     *
     * @return configured OpenAPI bean
     */
    @Bean
    public OpenAPI treasuryOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Treasury API (Reactive)")
                .description("Reactive Backend API for Treasury Module - RT-ComOps ERP\n\n" +
                    "Built with **Spring WebFlux** and **R2DBC** for non-blocking operations.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("RT-ComOps Team")
                    .email("support@rtcomops.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server (Reactive)")
            ));
    }
}