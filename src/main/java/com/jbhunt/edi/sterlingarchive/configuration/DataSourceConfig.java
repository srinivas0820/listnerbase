package com.jbhunt.edi.sterlingarchive.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean(destroyMethod = "")
    @Primary
    @RefreshScope
    @ConfigurationProperties(prefix = "jbhunt.b2b.datasource.SI2014_PRD")
    public DataSource siDataSource() {
        return DataSourceBuilder.create()
                .build();
    }

    @Bean
    public NamedParameterJdbcTemplate siJdbcTemplate(@Qualifier("siDataSource") DataSource siDataSource) {
        return new NamedParameterJdbcTemplate(siDataSource);
    }
}