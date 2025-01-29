package com.github.khalaimovda.controller;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.dto.PostUpdateContentForm;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public String getPosts(
        @RequestParam(name="page", defaultValue = "0") int pageNumber,
        @RequestParam(name="size", defaultValue = "10") int pageSize,
        @RequestParam(name="tag", required = false) Tag tag,
        Model model
    ) {
        Pageable pageable = Pageable.of(pageNumber, pageSize);
        Page<PostSummary> page = postService.getPostSummaryPage(pageable, tag);
        model.addAttribute("page", page);
        return "posts";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable(name = "id") Long id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        return "post";
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@Valid @ModelAttribute PostCreateForm form) {
        postService.createPost(form);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> addComment(
        @PathVariable(name = "id") Long postId,
        @RequestParam(name="text") String commentText
    ) {
        postService.addComment(postId, commentText);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePostContent(
        @PathVariable(name = "id") Long id,
        @Valid @ModelAttribute PostUpdateContentForm form
    ) {
        postService.updatePostContent(id, form);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable(name = "id") Long id) {
        postService.likePost(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable(name = "postId") Long postId,
        @PathVariable(name = "commentId") Long commentId,
        @RequestParam(name="text") String commentText
    ) {
        postService.updateComment(commentId, commentText);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable(name = "id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
