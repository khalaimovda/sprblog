package com.github.khalaimovda.data;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    @Value("${database.initialization.data.enabled:true}")
    private boolean dataInitEnabled;

    private final DataSource dataSource;

    @PostConstruct
    @Transactional
    public void init() {
        initializeSchema();
        if (dataInitEnabled) {
            initializeData();
        }
    }

    private void initializeSchema() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/schema.sql"));
        populator.execute(dataSource);
        System.out.println("Database schema was created");
    }

    private void initializeData() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/data.sql"));
        populator.execute(dataSource);
        System.out.println("Database initialized with default data");
    }
}
