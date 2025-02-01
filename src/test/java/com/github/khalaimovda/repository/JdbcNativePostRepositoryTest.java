package com.github.khalaimovda.repository;

import com.github.khalaimovda.config.DataSourceConfig;
import com.github.khalaimovda.data.DatabaseInitializer;
import com.github.khalaimovda.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(classes = {DataSourceConfig.class, JdbcNativePostRepository.class, DatabaseInitializer.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class JdbcNativePostRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        // Clear data
        jdbcTemplate.execute("DELETE FROM comments;");
        jdbcTemplate.execute("DELETE FROM post_tag;");
        jdbcTemplate.execute("DELETE FROM posts;");

        // Fill test data
        jdbcTemplate.execute(" INSERT INTO posts (id, title, text, image_path) VALUES(1, 'First', 'First text', 'first_image.jpg');");
    }

    @Test
    void testFindById() {
        Post post = postRepository.findById(1);

        assertNotNull(post);
        assertEquals(1L, post.getId());
        assertEquals("First", post.getTitle());
        assertEquals("First text", post.getText());
        assertEquals("first_image.jpg", post.getImagePath());
    }
}