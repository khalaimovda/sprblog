package com.github.khalaimovda.controller;

import com.github.khalaimovda.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringJUnitConfig(classes = {AppConfig.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Set Web Application Context
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clear data
        jdbcTemplate.execute("DELETE FROM comments;");
        jdbcTemplate.execute("DELETE FROM post_tag;");
        jdbcTemplate.execute("DELETE FROM posts;");

        // Fill test data
        jdbcTemplate.execute(" INSERT INTO posts (id, title, text, image_path) VALUES(1, 'First', 'First text', 'first_image.jpg');");
    }

    @Test
    void getPostById_shouldReturnHtmlWithPost() throws Exception {
        mockMvc.perform(get("/posts/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("post"))
            .andExpect(model().attributeExists("post"))
            .andExpect(content().string(containsString("<div id=\"postId\" hidden>1</div>")));
    }
}