package com.github.khalaimovda.controller;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("")
    public String getPosts(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "2") int size,
        @RequestParam(name="tag", required = false) Tag tag,
        Model model
    ) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postService.getPosts(pageable, tag);
        model.addAttribute("posts", posts);
        return "posts";
    }

    @PostMapping("")
    public String createPost(@Valid @ModelAttribute PostCreateForm form) {
        Post post = postService.createPost(form);
        return "hello";
    }
}
