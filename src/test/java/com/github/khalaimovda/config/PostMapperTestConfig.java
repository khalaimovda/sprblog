package com.github.khalaimovda.config;

import com.github.khalaimovda.mapper.PostMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;


@TestConfiguration
public class PostMapperTestConfig {
    @Bean
    public PostMapper postMapper() {
        return Mappers.getMapper(PostMapper.class);
    }
}
