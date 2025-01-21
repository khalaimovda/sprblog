package com.github.khalaimovda.controller;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/")
    public String getPosts(Model model) {
        List<Post> posts = postService.getPosts();
        posts.forEach(System.out::println);
        model.addAttribute("posts", posts);
        return "posts";
    }

    @GetMapping("/{name}")
    public String createPost(@PathVariable("name") String name) {
        Post post = postService.createPost(name);
        System.out.println(post);
        return "hello";
    }
}
