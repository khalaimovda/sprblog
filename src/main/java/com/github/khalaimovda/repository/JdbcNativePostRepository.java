package com.github.khalaimovda.repository;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<Post> findAll(Pageable pageable, Supplier<Tag> tagFilter) {
        int totalElements = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        String tagFilterStatement = tagFilter != null ?
            String.format("HAVING '%s' = ANY(pt.tags)", tagFilter.get().name())
            : "";

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
                    COUNT(c.id) AS comment_count
                FROM posts AS p
                LEFT JOIN comments AS c ON p.id = c.post_id
                GROUP BY p.id
            )
            SELECT
                p.title,
                p.text,
                p.image_path,
                p.likes,
                pt.tags,
                pc.comment_count
            FROM posts AS p
            JOIN post_tags AS pt ON p.id = pt.post_id
            JOIN post_comments AS pc ON p.id = pc.post_id
            %s
            ORDER BY p.created_at DESC
            LIMIT ? OFFSET ?
            """,
            tagFilterStatement
        );

        List<Post> posts = jdbcTemplate.query(
            query,
            (rs, rowNum) -> {
                Object sqlTags = rs.getArray("tags").getArray();
                Set<Tag> tags = Arrays.stream((Object[]) sqlTags)
                    .map(Object::toString)
                    .map(Tag::valueOf)
                    .collect(Collectors.toSet());
                return new Post(
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getString("image_path"),
                    rs.getInt("likes"),
                    tags,
                    rs.getInt("comment_count")
                );
            },
            pageable.getPageSize(), pageable.getOffset()
        );

        return Page.of(pageable.getPageNumber(), totalPages, posts);
    }
}
