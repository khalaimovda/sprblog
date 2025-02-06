package com.github.khalaimovda.service;

import com.github.khalaimovda.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public void addComment(long postId, String commentText) {
        commentRepository.addComment(postId, commentText);
    }

    @Override
    public void updateComment(long id, String text) {
        commentRepository.updateComment(id, text);
    }

    @Override
    public void deleteComment(long id) {
        commentRepository.deleteComment(id);
    }
}
