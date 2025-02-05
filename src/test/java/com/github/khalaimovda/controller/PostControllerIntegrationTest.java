package com.github.khalaimovda.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.github.khalaimovda.utils.DatabaseUtils.cleanData;
import static com.github.khalaimovda.utils.DatabaseUtils.fillData;
import static com.github.khalaimovda.utils.ImageUtils.cleanImages;
import static com.github.khalaimovda.utils.ImageUtils.createImageFilePath;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.sql.init.mode=always")
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

        cleanData(jdbcTemplate);
        cleanImages(resourceLoader);
        fillData(jdbcTemplate);
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
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);

        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("post"))
            .andExpect(model().attributeExists("post"))
            .andExpect(content().string(containsString(String.format("<div id=\"postId\" hidden>%s</div>", postId))))
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

        Path imageFilePath = createImageFilePath(resourceLoader, imagePath);
        String savedImageContent = Files.readString(imageFilePath);
        assertEquals(imageContent, savedImageContent);
    }

    @Test
    void testAddComment() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        String text = "New comment";

        mockMvc.perform(post(String.format("/posts/%s/comments", postId))
                .param("text", text))
            .andExpect(status().isCreated());

        Integer totalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = ?
            """,
            Integer.class,
            postId
        );
        assertNotNull(totalCount);
        assertEquals(3, totalCount);

        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = ? AND text = ?;
            """,
            Integer.class,
            postId, text
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testUpdatePostContent() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);

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

        mockMvc.perform(multipart("/posts/" + postId)
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
            "SELECT COUNT(*) FROM posts WHERE id = ? AND title = ? AND text = ? AND image_path LIKE ?",
            Integer.class,
            postId, title, text, "%" + imageFilename
        );
        assertNotNull(postCount);
        assertEquals(1, postCount);

        String imagePath = jdbcTemplate.queryForObject("SELECT image_path FROM posts WHERE id = ?", String.class, postId);

        Integer commentTotalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM comments WHERE post_id = ?
            """,
            Integer.class,
            postId
        );
        assertNotNull(commentTotalCount);
        assertEquals(2, commentTotalCount);

        Integer tagTotalCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM post_tag WHERE post_id = ?
            """,
            Integer.class,
            postId
        );
        assertNotNull(tagTotalCount);
        assertEquals(1, tagTotalCount);

        Integer tagCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM post_tag WHERE post_id = ?
            AND tag_id = (SELECT id FROM tags WHERE name = 'POLITICS');
            """,
            Integer.class,
            postId
        );
        assertNotNull(tagCount);
        assertEquals(1, tagCount);

        Path prevImageFilePath = createImageFilePath(resourceLoader, "first_image.jpg");
        assertFalse(Files.exists(prevImageFilePath));

        Path imageFilePath = createImageFilePath(resourceLoader, imagePath);
        String savedImageContent = Files.readString(imageFilePath);
        assertEquals(imageContent, savedImageContent);
    }

    @Test
    void testLikePost() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        mockMvc.perform(post(String.format("/posts/%s/like", postId))).andExpect(status().isOk());

        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM posts WHERE id = ?; ", Integer.class, postId);
        assertNotNull(likes);
        assertEquals(4, likes);
    }

    @Test
    void testUpdateComment() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        Long commentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'First comment of first post';", Long.class);

        String text = "New comment text";

        mockMvc.perform(put(String.format("/posts/%s/comments/%s", postId, commentId))
                .param("text", text))
            .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id = ? AND text = ?;",
            Integer.class,
            commentId, text
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testDeletePost() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        mockMvc.perform(delete("/posts/" + postId)).andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts WHERE id = ?;", Integer.class, postId);
        assertNotNull(count);
        assertEquals(0, count);
    }

    @Test
    void testDeleteComment() throws Exception {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        Long commentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'First comment of first post';", Long.class);

        mockMvc.perform(delete(String.format("/posts/%s/comments/%s", postId, commentId))).andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments WHERE id = ?;", Integer.class, commentId);
        assertNotNull(count);
        assertEquals(0, count);
    }
}