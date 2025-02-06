package com.github.khalaimovda.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.github.khalaimovda.utils.DatabaseUtils.cleanData;
import static com.github.khalaimovda.utils.DatabaseUtils.fillData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@JdbcTest
@Import(JdbcNativeCommentRepository.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.sql.init.mode=always")
class JdbcNativeCommentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        cleanData(jdbcTemplate);
        fillData(jdbcTemplate);
    }

    @Test
    void testAddComment() {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        String commentText = "New comment";

        commentRepository.addComment(postId, commentText);

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
            postId, commentText
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testUpdateComment() {
        Long commentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'First comment of first post';", Long.class);
        commentRepository.updateComment(commentId, "New comment text");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id = ? AND text = 'New comment text';",
            Integer.class,
            commentId
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testDeleteComment() {
        Long commentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'First comment of first post';", Long.class);
        commentRepository.deleteComment(commentId);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments WHERE id = ?;", Integer.class, commentId);
        assertNotNull(count);
        assertEquals(0, count);
    }
}