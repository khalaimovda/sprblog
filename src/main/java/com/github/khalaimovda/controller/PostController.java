package com.github.khalaimovda.controller;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
        Page<Post> posts = postService.getPosts(pageable, tag);
        model.addAttribute("page", posts);
        return "posts";
    }

//    @PostMapping
//    public ResponseEntity<Void> createPost(@Valid @ModelAttribute PostCreateForm form) {
//        try {
//            String imagePath = imageService.saveImage(form.getImage());
//            postService.createPost(form, imagePath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
}
