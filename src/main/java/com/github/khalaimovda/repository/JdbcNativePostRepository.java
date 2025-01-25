package com.github.khalaimovda.repository;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<Post> findAll(Pageable pageable) {
        int totalElements = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        // todo: ORDER BY created_at DESC
        // todo: comments -- count of comments
        // todo: tags
        List<Post> posts = jdbcTemplate.query(
            "SELECT title, text, image_path, likes FROM posts LIMIT ? OFFSET ?",
            (rs, rowNum) -> new Post(
                rs.getString("title"),
                rs.getString("text"),
                rs.getString("image_path"),
                rs.getInt("likes")
            ),
            pageable.getPageSize(), pageable.getOffset()
        );

        return Page.of(pageable.getPageNumber(), totalPages, posts);
    }
}
