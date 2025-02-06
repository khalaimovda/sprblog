package com.github.khalaimovda.service;

import com.github.khalaimovda.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CommentServiceImpl.class)
@ActiveProfiles("test")
class CommentServiceTest {
    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @BeforeEach
    void resetMocks() {
        reset(commentRepository);
    }

    @Test
    void testAddComment() {
        doNothing().when(commentRepository).addComment(anyLong(), anyString());
        commentService.addComment(13L, "New comment text");
        verify(commentRepository).addComment(13L, "New comment text");
    }

    @Test
    void testUpdateComment() {
        doNothing().when(commentRepository).updateComment(anyLong(), anyString());
        commentService.updateComment(13L, "Updated comment text");
        verify(commentRepository).updateComment(13L, "Updated comment text");
    }

    @Test
    void testDeleteComment() {
        doNothing().when(commentRepository).deleteComment(anyLong());
        commentService.deleteComment(13L);
        verify(commentRepository).deleteComment(13L);
    }
}