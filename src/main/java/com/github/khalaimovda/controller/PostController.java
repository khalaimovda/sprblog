package com.github.khalaimovda.controller;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("")
    public String getPosts(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "2") int size,
        Model model
    ) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postService.getPosts(pageable);
        model.addAttribute("posts", posts);
        return "posts";
    }

    @GetMapping("/{name}")
    public String createPost(@PathVariable("name") String name) {
        Post post = postService.createPost(name);
        return "hello";
    }
}
