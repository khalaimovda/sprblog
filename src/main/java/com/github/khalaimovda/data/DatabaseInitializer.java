package com.github.khalaimovda.data;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final DataSource dataSource;

    @PostConstruct
    @Transactional
    public void init() {
        initializeData();
    }

    private void initializeData() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/schema.sql"));
        populator.execute(dataSource);
        System.out.println("Database initialized with default data");
    }
}
