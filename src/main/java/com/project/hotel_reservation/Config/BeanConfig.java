package com.project.hotel_reservation.Config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
public class BeanConfig {
    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        try {
            builder.url("jdbc:postgresql://localhost:5432/hotel");
            builder.username("postgres");
            builder.password("anime021");
            builder.driverClassName("org.postgresql.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

}
