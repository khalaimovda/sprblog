package com.github.khalaimovda;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.github.khalaimovda")
@EnableJpaRepositories(basePackages = "com.github.khalaimovda.repository")
@PropertySource("classpath:application.properties")
public class AppConfig  {
}
