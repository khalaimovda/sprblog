package com.github.khalaimovda.config;


import com.github.khalaimovda.service.ImageService;
import com.github.khalaimovda.service.ImageServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ImageServiceTestConfig {

    @Bean
    public ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }

    @Bean
    public Path imageLocation(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        if (!resource.exists()) {
            throw new IllegalStateException("Test folder not found in classpath");
        }
        return Paths.get(resource.getURI());
    }

    @Bean
    public ImageService imageService(ResourceLoader resourceLoader) throws IOException {
        return new ImageServiceImpl(resourceLoader);
    }
}
