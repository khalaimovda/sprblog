package com.github.khalaimovda.repository;

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
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static com.github.khalaimovda.utils.DatabaseUtils.cleanData;
import static com.github.khalaimovda.utils.DatabaseUtils.fillData;
import static org.junit.jupiter.api.Assertions.*;


@JdbcTest
@Import(JdbcNativePostRepository.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.sql.init.mode=always")
class JdbcNativePostRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        cleanData(jdbcTemplate);
        fillData(jdbcTemplate);
    }

    @Test
    void testFindAllSummariesPageableFirstPageWithoutTagFilter() {
        Long fistPostId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        Long secondPostId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'Second';", Long.class);

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
            new PostSummary(secondPostId, "Second", "Second text", "second_image.jpg", 15, Set.of(), 0),
            new PostSummary(fistPostId, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE), 2)
        );

        assertNotNull(page);
        assertEquals(1, page.totalPages());
        assertEquals(0, page.number());

        assertIterableEquals(expectedContent, page.content());
    }


    @Test
    void testFindAllSummariesPageableFirstPageWithTagFilter() {
        Long fistPostId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
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
            new PostSummary(fistPostId, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE), 2)
        );

        assertNotNull(page);
        assertEquals(1, page.totalPages());
        assertEquals(0, page.number());

        assertIterableEquals(expectedContent, page.content());
    }

    @Test
    void testFindById() {
        Long fistPostId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        Long firstCommentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'First comment of first post';", Long.class);
        Long secondCommentId = jdbcTemplate.queryForObject("SELECT id FROM comments WHERE text = 'Second comment of first post';", Long.class);


        Post expectedPost = new Post(fistPostId, "First", "First text", "first_image.jpg", 3, Set.of(Tag.SCIENCE));
        expectedPost.setComments(List.of(
            new Comment(secondCommentId, "Second comment of first post"),
            new Comment(firstCommentId, "First comment of first post")
        ));

        Post post = postRepository.findById(fistPostId);

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
        Post post = postRepository.findById(Long.MAX_VALUE / 2);
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
    void testUpdateContentWithoutImagePath() {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        PostUpdateContentDto dto = new PostUpdateContentDto(postId, "New title", "New text", Set.of(Tag.POLITICS));

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
    }

    @Test
    void testUpdateContentWithImagePath() {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        PostUpdateContentDto dto = new PostUpdateContentDto(postId, "New title", "New text", Set.of(Tag.POLITICS));
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
    }

    @Test
    void testIncrementLikes() {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        postRepository.incrementLikes(postId);

        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM posts WHERE id = ?; ", Integer.class, postId);
        assertNotNull(likes);
        assertEquals(4, likes);
    }

    @Test
    void testDeletePost() {
        Long postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE title = 'First';", Long.class);
        postRepository.deletePost(postId);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts WHERE id = ?;", Integer.class, postId);
        assertNotNull(count);
        assertEquals(0, count);
    }
}