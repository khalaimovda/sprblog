package com.github.khalaimovda.service;

public interface CommentService {
    void addComment(long postId, String commentText);
    void updateComment(long id, String text);
    void deleteComment(long id);
}
