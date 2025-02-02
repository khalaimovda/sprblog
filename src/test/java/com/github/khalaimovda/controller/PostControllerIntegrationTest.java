package com.github.khalaimovda.controller;

import com.github.khalaimovda.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringJUnitConfig(classes = {AppConfig.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws IOException {
        // Set Web Application Context
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        cleanData();
        cleanImages();
        fillData();
    }

    void cleanData() {
        jdbcTemplate.execute("DELETE FROM comments;");
        jdbcTemplate.execute("DELETE FROM post_tag;");
        jdbcTemplate.execute("DELETE FROM posts;");
    }

    void fillData() {
        jdbcTemplate.execute(" INSERT INTO posts (id, title, text, image_path, likes) VALUES(1, 'First', 'First text', 'first_image.jpg', 3);");
        jdbcTemplate.execute(" INSERT INTO posts (id, title, text, image_path, likes) VALUES(2, 'Second', 'Second text', 'second_image.jpg', 15);");

        jdbcTemplate.execute(
            """
                INSERT INTO comments (id, text, post_id)
                SELECT 1, 'First comment of first post', id
                FROM posts WHERE title = 'First';
                """
        );
        jdbcTemplate.execute(
            """
                INSERT INTO comments (id, text, post_id)
                SELECT 2, 'Second comment of first post', id
                FROM posts WHERE title = 'First';
                """
        );

        jdbcTemplate.execute(
            """
                INSERT INTO post_tag (post_id, tag_id)
                SELECT
                    (SELECT id FROM posts WHERE title = 'First'),
                    (SELECT id FROM tags WHERE name = 'SCIENCE');
                """
        );
    }

    void cleanImages() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        Path imageLocation = Paths.get(resource.getURI());

        if (Files.exists(imageLocation)) {
            Files.walk(imageLocation)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Clean test image directory error", e);
                    }
                });
        }
        Files.createDirectories(imageLocation);
    }

    Path createImageFilePath(String imageFilename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        Path imageLocation = Paths.get(resource.getURI());
        return imageLocation.resolve(imageFilename);
    }

    @Test
    void testGetPostsShouldReturnHtmlWithPosts() throws Exception {
        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("posts"))
            .andExpect(model().attributeExists("page"))

            .andExpect(content().string(containsString("<h2>First</h2>")))
            .andExpect(content().string(containsString("<h2>Second</h2>")))

            .andExpect(content().string(containsString("First text")))
            .andExpect(content().string(containsString("Second text")))

            .andExpect(content().string(containsString("images/first_image.jpg")))
            .andExpect(content().string(containsString("images/second_image.jpg")))

            .andExpect(content().string(containsString("<div class=\"tag\">SCIENCE</div>")))

            .andExpect(content().string(containsString("Комментариев: <span>0</span>")))
            .andExpect(content().string(containsString("Комментариев: <span>2</span>")))

            .andExpect(content().string(containsString("Лайков: <span>3</span>")))
            .andExpect(content().string(containsString("Лайков: <span>15</span>")));
    }

    @Test
    void testGetPostByIdShouldReturnHtmlWithPost() throws Exception {
        mockMvc.perform(get("/posts/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("post"))
            .andExpect(model().attributeExists("post"))
            .andExpect(content().string(containsString("<div id=\"postId\" hidden>1</div>")))
            .andExpect(content().string(containsString("<h2>First</h2>")))
            .andExpect(content().string(containsString("First text")))
            .andExpect(content().string(containsString("images/first_image.jpg")))
            .andExpect(content().string(containsString("<div class=\"tag\">SCIENCE</div>")))
            .andExpect(content().string(containsString("Лайков: 3")))
            .andExpect(content().string(containsString("<div class=\"tag\">SCIENCE</div>")))
            .andExpect(content().string(containsString("First comment of first post")))
            .andExpect(content().string(containsString("Second comment of first post")));
    }

    @Test
    void testCreatePost() throws Exception {
        String title = "New title";
        String text = "New text";
        String imageFilename = "test-image.jpg";
        String imageContent = "fake image content";

        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            imageFilename,
            "image/jpeg",
            imageContent.getBytes()
        );

        mockMvc.perform(multipart("/posts")
                .file(imageFile)
                .param("title", title)
                .param("text", text)
                .param("tags", "ART")
                .param("tags", "POLITICS"))
            .andExpect(status().isCreated());

        Integer postCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM posts WHERE title = ? AND text = ? AND image_path LIKE ?",
            Integer.class,
            title, text, "%" + imageFilename
        );
        assertNotNull(postCount);
        assertEquals(1, postCount);

        Map.Entry<Long, String> result = jdbcTemplate.queryForObject(
            "SELECT id, image_path FROM posts WHERE title = ? AND text = ? AND image_path LIKE ?",
            (rs, rowNum) -> Map.entry(rs.getLong("id"), rs.getString("image_path")),
            title, text, "%" + imageFilename
        );
        Long postId = result.getKey();
        String imagePath = result.getValue();

        Integer tagCount = jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) FROM post_tag WHERE post_id = ?;
                """,
            Integer.class,
            postId
        );
        assertNotNull(tagCount);
        assertEquals(2, tagCount);

        Integer artTagCount = jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) FROM post_tag WHERE
                    tag_id = (SELECT id FROM tags WHERE name = 'ART') AND
                    post_id = ?
                """,
            Integer.class,
            postId
        );
        assertNotNull(artTagCount);
        assertEquals(1, artTagCount);

        Integer religionTagCount = jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*) FROM post_tag WHERE
                    tag_id = (SELECT id FROM tags WHERE name = 'POLITICS') AND
                    post_id = ?
                """,
            Integer.class,
            postId
        );
        assertNotNull(religionTagCount);
        assertEquals(1, religionTagCount);

        Path imageFilePath = createImageFilePath(imagePath);
        String savedImageContent = Files.readString(imageFilePath);
        assertEquals(imageContent, savedImageContent);
    }

    @Test
    void testAddComment() throws Exception {
        String text = "New comment";

        mockMvc.perform(post("/posts/1/comments")
                .param("text", text))
            .andExpect(status().isCreated());

        Integer totalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = ?
            """,
            Integer.class,
            1
        );
        assertNotNull(totalCount);
        assertEquals(3, totalCount);

        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = ? AND text = ?;
            """,
            Integer.class,
            1, text
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testUpdatePostContent() throws Exception {
        String title = "New title";
        String text = "New text";
        String imageFilename = "updated_image.jpg";
        String imageContent = "Updated image content";

        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            imageFilename,
            "image/jpeg",
            imageContent.getBytes()
        );

        mockMvc.perform(multipart("/posts/1")
                .file(imageFile)
                .param("title", title)
                .param("text", text)
                .param("tags", "POLITICS")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isOk());

        Integer postCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM posts WHERE id = 1 AND title = ? AND text = ? AND image_path LIKE ?",
            Integer.class,
            title, text, "%" + imageFilename
        );
        assertNotNull(postCount);
        assertEquals(1, postCount);

        String imagePath = jdbcTemplate.queryForObject("SELECT image_path FROM posts WHERE id = 1", String.class);

        Integer commentTotalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = 1
            """,
            Integer.class
        );
        assertNotNull(commentTotalCount);
        assertEquals(2, commentTotalCount);

        Integer tagTotalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM post_tag WHERE post_id = 1
            """,
            Integer.class
        );
        assertNotNull(tagTotalCount);
        assertEquals(1, tagTotalCount);

        Integer tagCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM post_tag WHERE post_id = 1
            AND tag_id = (SELECT id FROM tags WHERE name = 'POLITICS'); 
            """,
            Integer.class
        );
        assertNotNull(tagCount);
        assertEquals(1, tagCount);

        Path prevImageFilePath = createImageFilePath("first_image.jpg");
        assertFalse(Files.exists(prevImageFilePath));

        Path imageFilePath = createImageFilePath(imagePath);
        String savedImageContent = Files.readString(imageFilePath);
        assertEquals(imageContent, savedImageContent);
    }

    @Test
    void testLikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like")).andExpect(status().isOk());

        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM posts WHERE id = 1; ", Integer.class);
        assertNotNull(likes);
        assertEquals(4, likes);
    }

    @Test
    void testUpdateComment() throws Exception {
        String text = "New comment text";

        mockMvc.perform(put("/posts/1/comments/1")
                .param("text", text))
            .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id = 1 AND text = ?;",
            Integer.class,
            text
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/1")).andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts WHERE id = 1;", Integer.class);
        assertNotNull(count);
        assertEquals(0, count);
    }

    @Test
    void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/posts/1/comments/1")).andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments WHERE id = 1;", Integer.class);
        assertNotNull(count);
        assertEquals(0, count);
    }
}