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

        String tagFilterStatement = tagFilter != null ?
            String.format("WHERE '%s' = ANY(pt.tags)", tagFilter.get().name())
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
                    COUNT(c.id) AS comments
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
        List<Post> posts = jdbcTemplate.query(
            query,
            (rs, rowNum) -> {
                totalRecords[0] = rs.getInt("total");

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
                    rs.getInt("comments")
                );
            },
            pageable.getPageSize(), pageable.getOffset()
        );

        int totalPages = (int) Math.ceil((double) totalRecords[0] / pageable.getPageSize());
        return Page.of(pageable.getPageNumber(), totalPages, posts);
    }
}
