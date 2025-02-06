package com.github.khalaimovda.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addComment(long postId, String commentText) {
        String query = """
            INSERT INTO comments (text, post_id)
            VALUES (?, ?);
        """;
        jdbcTemplate.update(query, commentText, postId);
    }

    @Override
    public void updateComment(long id, String text) {
        jdbcTemplate.update("UPDATE comments SET text = ? WHERE id = ?", text, id);
    }

    @Override
    public void deleteComment(long id) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?;", id);
    }
}
