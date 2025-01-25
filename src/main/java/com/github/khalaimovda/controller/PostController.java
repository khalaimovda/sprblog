package com.github.khalaimovda.controller;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.service.ImageService;
import com.github.khalaimovda.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


// todo: Вынести ImageService внутрь PostService

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ImageService imageService;

    @GetMapping
    public String getPosts(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "20") int size,
        @RequestParam(name="tag", required = false) Tag tag,
        Model model
    ) {
        Pageable pageable = Pageable.of(page, size);
        Page<Post> posts = postService.getPosts(pageable, tag);
//        posts.forEach(post -> post.setImagePath(imageService.getImageSrcPath(post.getImagePath())));
        model.addAttribute("posts", posts);
        return "posts";
    }

//    @PostMapping
//    public String createPost(@Valid @ModelAttribute PostCreateForm form) {
//        try {
//            String imagePath = imageService.saveImage(form.getImage());
//            postService.createPost(form, imagePath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//            // todo: error page
//        }
//        // todo: redirect to getPosts()
//        return "hello";
//    }
}
