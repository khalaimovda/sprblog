package com.github.khalaimovda.repository;

import com.github.khalaimovda.dto.PostCreateDto;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.dto.PostUpdateContentDto;
import com.github.khalaimovda.model.Comment;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<PostSummary> findAllSummariesPageable(Pageable pageable, @Nullable Tag tag) {

        String tagFilterStatement = tag != null ? String.format("WHERE '%s' = ANY(pt.tags)", tag.name()) : "";

        String query = String.format("""
            WITH post_tags AS (
                SELECT
                    p.id AS post_id,
                    COALESCE(
                        ARRAY_AGG(t.name) FILTER (WHERE t.name IS NOT NULL),
                        ARRAY[]
                    ) AS tags
                FROM posts AS p
                LEFT JOIN post_tag AS pt ON p.id = pt.post_id
                LEFT JOIN tags AS t ON pt.tag_id = t.id
                GROUP BY p.id
            ),
            post_comments AS (
                SELECT
                    p.id AS post_id,
                    COUNT(c.id) AS comments
                FROM posts AS p
                LEFT JOIN comments AS c ON p.id = c.post_id
                GROUP BY p.id
            )
            SELECT
                p.id,
                p.title,
                p.text,
                p.image_path,
                p.likes,
                pt.tags,
                pc.comments,
                COUNT(*) OVER () AS total
            FROM posts AS p
            JOIN post_tags AS pt ON p.id = pt.post_id
            JOIN post_comments AS pc ON p.id = pc.post_id
            %s
            ORDER BY p.created_at DESC
            LIMIT ? OFFSET ?
            """,
            tagFilterStatement
        );

        int[] totalRecords = new int[1];
        List<PostSummary> posts = jdbcTemplate.query(
            query,
            (rs, rowNum) -> {
                totalRecords[0] = rs.getInt("total");

                Object sqlTags = rs.getArray("tags").getArray();
                Set<Tag> tags = Arrays.stream((Object[]) sqlTags)
                    .map(Object::toString)
                    .map(Tag::valueOf)
                    .collect(Collectors.toSet());

                return new PostSummary(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getString("image_path"),
                    rs.getInt("likes"),
                    tags,
                    rs.getInt("comments")
                );
            },
            pageable.getPageSize(), pageable.getOffset()
        );

        int totalPages = (int) Math.ceil((double) totalRecords[0] / pageable.getPageSize());
        return Page.of(pageable.getPageNumber(), totalPages, posts);
    }

    @Override
    @Transactional
    public void create(PostCreateDto dto) {
        // Create new post
        Long postId = jdbcTemplate.queryForObject(
            "SELECT id FROM FINAL TABLE ( INSERT INTO posts (title, text, image_path) VALUES(?, ?, ?))",
            Long.class,
            dto.getTitle(), dto.getText(), dto.getImagePath()
        );

        // Link post to its tags
        addTags(postId, dto.getTags());
    }

    @Override
    public @Nullable Post findById(long id) {

        Post post;
        String query = """
            SELECT
                p.title,
                p.text,
                p.image_path,
                p.likes,
                COALESCE(
                    ARRAY_AGG(t.name) FILTER (WHERE t.name IS NOT NULL),
                    ARRAY[]
                ) AS tags
            FROM posts AS p
            LEFT JOIN post_tag AS pt ON p.id = pt.post_id
            LEFT JOIN tags AS t ON pt.tag_id = t.id
            WHERE p.id = ?
            GROUP BY p.id
        """;
        try {
            post = jdbcTemplate.queryForObject(
                query,
                (rs, rowNum) -> {
                    Object sqlTags = rs.getArray("tags").getArray();
                    Set<Tag> tags = Arrays.stream((Object[]) sqlTags)
                        .map(Object::toString)
                        .map(Tag::valueOf)
                        .collect(Collectors.toSet());

                    return new Post(
                        id,
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("image_path"),
                        rs.getInt("likes"),
                        tags
                    );
                },
                id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

        List<Comment> comments = findComments(id);

        post.setComments(comments);
        return post;
    }

    @Override
    public void addComment(long postId, String commentText) {
        String query = """
            INSERT INTO comments (text, post_id)
            VALUES (?, ?);
        """;
        jdbcTemplate.update(query, commentText, postId);
    }

    @Override
    @Transactional
    public void updateContent(PostUpdateContentDto dto) {
        jdbcTemplate.update(
            "UPDATE posts SET title = ?, text = ? WHERE id = ?;",
            dto.getTitle(), dto.getText(), dto.getId()
        );
        updateTags(dto.getId(), dto.getTags());
    }

    @Override
    @Transactional
    public String updateContent(PostUpdateContentDto dto, String imagePath) {
        String prevImagePath = jdbcTemplate.queryForObject(
            "SELECT image_path FROM OLD TABLE (UPDATE posts SET title = ?, text = ?, image_path = ? WHERE id = ?);",
            String.class,
            dto.getTitle(), dto.getText(), imagePath, dto.getId()
        );
        updateTags(dto.getId(), dto.getTags());
        return prevImagePath;
    }

    @Override
    public void incrementLikes(long id) {
        jdbcTemplate.update("UPDATE posts SET likes = likes + 1 WHERE id = ?", id);
    }

    @Override
    public void updateComment(long id, String text) {
        jdbcTemplate.update("UPDATE comments SET text = ? WHERE id = ?", text, id);
    }

    @Override
    @Transactional
    public String deletePost(long id) {
        // Delete comments
        jdbcTemplate.update("DELETE FROM comments WHERE post_id = ?;", id);

        // Delete tags
        jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?;", id);

        // Delete post
        return jdbcTemplate.queryForObject(
            "SELECT image_path FROM OLD TABLE (DELETE FROM posts WHERE id = ? );",
            String.class,
            id
        );
    }

    @Override
    public void deleteComment(long id) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?;", id);
    }

    private List<Comment> findComments(long postId) {
        String query = """
            SELECT c.id, c.text
            FROM  comments AS c
            JOIN posts AS p ON c.post_id = p.id
            WHERE p.id = ?
            ORDER BY c.created_at DESC;
        """;
        return jdbcTemplate.query(
            query,
            (rs, rowNum) -> new Comment(
                rs.getLong("id"),
                rs.getString("text")
            ),
            postId
        );
    }

    private void addTags(long postId, Set<Tag> tags) {
        if (tags.isEmpty()) {
            return;
        }
        String tagQuerySet = "(" +
            String.join(",", tags.stream().map(tag -> "'" + tag.name() + "'").toList()) +
            ")";
        String linkTagsQuery =
            "INSERT INTO post_tag (post_id, tag_id) " +
                "SELECT ?, id FROM tags WHERE name IN " +
                tagQuerySet;
        jdbcTemplate.update(linkTagsQuery, postId);
    }

    @Transactional
    private void updateTags(long postId, Set<Tag> tags) {
        jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", postId);
        addTags(postId, tags);
    }
}
