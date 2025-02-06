package com.github.khalaimovda.repository;

public interface CommentRepository {
    void addComment(long postId, String commentText);
    void updateComment(long id, String text);
    void deleteComment(long id);
}
