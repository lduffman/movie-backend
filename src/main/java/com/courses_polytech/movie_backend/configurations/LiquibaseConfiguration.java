package com.courses_polytech.movie_backend.configurations;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.liquibase")
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean("liquibase")
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties liquibaseProperties) {
        {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog(liquibaseProperties.getChangeLog());
            liquibase.setContexts(String.valueOf(liquibaseProperties.getContexts()));
            liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
            liquibase.setDropFirst(liquibaseProperties.isDropFirst());
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
            liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
            liquibase.setTag(liquibaseProperties.getTag());
            return liquibase;
        }
    }
}
