package com.rtcomops.treasury.infrastructure.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuration for Liquibase database migrations.
 *
 * <p>This configuration creates a JDBC DataSource specifically for Liquibase,
 * since the application uses R2DBC for reactive database access.</p>
 *
 * <p>This configuration is part of the infrastructure layer in the hexagonal architecture.</p>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-11
 */
@Configuration
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriverClassName;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    /**
     * Creates a DataSource bean for Liquibase migrations.
     *
     * @return configured DataSource
     */
    @Bean
    public DataSource liquibaseDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(datasourceDriverClassName);
        dataSource.setUrl(datasourceUrl);
        dataSource.setUsername(datasourceUsername);
        dataSource.setPassword(datasourcePassword);
        return dataSource;
    }

    /**
     * Creates and configures SpringLiquibase bean.
     *
     * @return configured SpringLiquibase instance
     */
    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(liquibaseDataSource());
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
