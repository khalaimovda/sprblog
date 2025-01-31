package com.github.khalaimovda.config;


import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.repository.PostRepository;
import com.github.khalaimovda.service.ImageService;
import com.github.khalaimovda.service.PostService;
import com.github.khalaimovda.service.PostServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(basePackages = "com.github.khalaimovda.mapper")
public class TestConfig {

    @Autowired
    private PostMapper postMapper;

    @Bean
    public PostService postService(PostRepository postRepository, ImageService imageService, PostMapper postMapper) {
        return new PostServiceImpl(postRepository, imageService, postMapper);
    }

    @Bean
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }

    @Bean
    public ImageService imageService() {
        return mock(ImageService.class);
    }
}
