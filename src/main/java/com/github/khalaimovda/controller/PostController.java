package com.github.khalaimovda.controller;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
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
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "10") int size,
        @RequestParam(name="tag", required = false) Tag tag,
        Model model
    ) {
        Pageable pageable = Pageable.of(page, size);
        Page<PostSummary> posts = postService.getPosts(pageable, tag);
        model.addAttribute("page", posts);
        return "posts";
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@Valid @ModelAttribute PostCreateForm form) {
        postService.createPost(form);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable(name = "id") Long id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        return "post";
    }
}
