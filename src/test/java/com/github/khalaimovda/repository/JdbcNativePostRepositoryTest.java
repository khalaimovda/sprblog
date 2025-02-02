package com.github.khalaimovda.repository;

import com.github.khalaimovda.config.DataSourceConfig;
import com.github.khalaimovda.data.DatabaseInitializer;
import com.github.khalaimovda.dto.PostCreateDto;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.dto.PostUpdateContentDto;
import com.github.khalaimovda.model.Comment;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

        // Fill data
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

    @Test
    void testFindAllSummariesPageableFirstPageWithoutTagFilter() {
        Page<PostSummary> page = postRepository.findAllSummariesPageable(
            new Pageable() {
                @Override
                public int getPageNumber() {
                    return 0;
                }

                @Override
                public int getPageSize() {
                    return 10;
                }

                @Override
                public int getOffset() {
                    return 0;
                }
            },
            null
        );

        List<PostSummary> expectedContent = List.of(
            new PostSummary(2L, "Second", "Second text", "second_image.jpg", 15, Set.of(), 0),
            new PostSummary(1L, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE), 2)
        );

        assertNotNull(page);
        assertEquals(1, page.totalPages());
        assertEquals(0, page.number());

        assertIterableEquals(expectedContent, page.content());
    }


    @Test
    void testFindAllSummariesPageableFirstPageWithTagFilter() {
        Page<PostSummary> page = postRepository.findAllSummariesPageable(
            new Pageable() {
                @Override
                public int getPageNumber() {
                    return 0;
                }

                @Override
                public int getPageSize() {
                    return 10;
                }

                @Override
                public int getOffset() {
                    return 0;
                }
            },
            Tag.SCIENCE
        );

        List<PostSummary> expectedContent = List.of(
            new PostSummary(1L, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE), 2)
        );

        assertNotNull(page);
        assertEquals(1, page.totalPages());
        assertEquals(0, page.number());

        assertIterableEquals(expectedContent, page.content());
    }

    @Test
    void testFindById() {
        Post expectedPost = new Post(1L, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE));
        expectedPost.setComments(List.of(
            new Comment(2L, "Second comment of first post"),
            new Comment(1L, "First comment of first post")
        ));

        Post post = postRepository.findById(1);

        assertNotNull(post);
        assertEquals(expectedPost.getId(), post.getId());
        assertEquals(expectedPost.getTitle(), post.getTitle());
        assertEquals(expectedPost.getText(), post.getText());
        assertEquals(expectedPost.getImagePath(), post.getImagePath());
        assertEquals(expectedPost.getLikes(), post.getLikes());
        assertEquals(expectedPost.getTags(), post.getTags());

        assertEquals(expectedPost.getComments().size(), post.getComments().size());
        for (int i = 0; i < expectedPost.getComments().size(); i++) {
            Comment expectedComment= expectedPost.getComments().get(i);
            Comment comment = post.getComments().get(i);
            assertEquals(expectedComment.getId(), comment.getId());
            assertEquals(expectedComment.getText(), comment.getText());
        }
    }

    @Test
    void testFindByIdNotFound() {
        Post post = postRepository.findById(3);
        assertNull(post);
    }

    @Test
    void testCreatePost() {
        PostCreateDto dto = new PostCreateDto("New title", "New text", "new_image.jpg", Set.of(Tag.ART, Tag.RELIGION));

        postRepository.create(dto);

        Integer postCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM posts WHERE title = ? AND text = ? AND image_path = ?",
            Integer.class,
            dto.getTitle(), dto.getText(), dto.getImagePath()
        );
        assertNotNull(postCount);
        assertEquals(1, postCount);

        Long postId = jdbcTemplate.queryForObject(
            "SELECT id FROM posts WHERE title = ? AND text = ? AND image_path = ?",
            Long.class,
            dto.getTitle(), dto.getText(), dto.getImagePath()
        );

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
                tag_id = (SELECT id FROM tags WHERE name = 'RELIGION') AND
                post_id = ?
            """,
            Integer.class,
            postId
        );
        assertNotNull(religionTagCount);
        assertEquals(1, religionTagCount);
    }

    @Test
    void testAddComment() {
        long postId = 1L;
        String commentText = "New comment";

        postRepository.addComment(postId, commentText);

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
    void testUpdateContentWithoutImagePath() {
        PostUpdateContentDto dto = new PostUpdateContentDto(1L, "New title", "New text", Set.of(Tag.POLITICS));

        postRepository.updateContent(dto);

        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM posts WHERE id = ? AND title = ? AND text = ? AND image_path = 'first_image.jpg' AND likes = 3;
            """,
            Integer.class,
            dto.getId(), dto.getTitle(), dto.getText()
        );
        assertNotNull(count);
        assertEquals(1, count);

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
    }

    @Test
    void testUpdateContentWithImagePath() {
        PostUpdateContentDto dto = new PostUpdateContentDto(1L, "New title", "New text", Set.of(Tag.POLITICS));
        String imagePath = "new_image_path.jpg";

        postRepository.updateContent(dto, imagePath);

        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM posts WHERE id = ? AND title = ? AND text = ? AND image_path = ? AND likes = 3;
            """,
            Integer.class,
            dto.getId(), dto.getTitle(), dto.getText(), imagePath
        );
        assertNotNull(count);
        assertEquals(1, count);

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
    }

    @Test
    void testIncrementLikes() {
        postRepository.incrementLikes(1L);

        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM posts WHERE id = 1; ", Integer.class);
        assertNotNull(likes);
        assertEquals(4, likes);
    }

    @Test
    void testUpdateComment() {
        postRepository.updateComment(1L, "New comment text");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id = 1 AND text = 'New comment text';",
            Integer.class
        );
        assertNotNull(count);
        assertEquals(1, count);
    }

    @Test
    void testDeletePost() {
        postRepository.deletePost(1L);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts WHERE id = 1;", Integer.class);
        assertNotNull(count);
        assertEquals(0, count);
    }

    @Test
    void testDeleteComment() {
        postRepository.deleteComment(1L);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments WHERE id = 1;", Integer.class);
        assertNotNull(count);
        assertEquals(0, count);
    }
}