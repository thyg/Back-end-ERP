package com.rtcomops.treasury.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * R2DBC Configuration for reactive database access.
 *
 * <p>Configures R2DBC repositories and transaction management
 * for non-blocking database operations with PostgreSQL.</p>
 *
 * <p>This configuration is part of the infrastructure layer in the hexagonal architecture.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-10
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.rtcomops.treasury.infrastructure.adapter.outgoing.persistence")
@EnableR2dbcAuditing
@EnableTransactionManagement
public class R2dbcConfig {

    /**
     * Creates a reactive transaction manager for R2DBC.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return configured ReactiveTransactionManager
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}

