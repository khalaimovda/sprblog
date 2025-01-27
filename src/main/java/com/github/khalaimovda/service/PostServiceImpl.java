package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostMapper postMapper;

    @Override
    public Page<PostSummary> getPosts(Pageable pageable, Tag tag) {
        Supplier<Tag> tagFilter = tag != null ? () -> tag : null;
        Page<PostSummary> posts = postRepository.findAll(pageable, tagFilter);
        posts.content().forEach(post -> post.setImagePath(imageService.getImageSrcPath(post.getImagePath())));
        return posts;
    }

    @Override
    public void createPost(PostCreateForm form) {
        String imagePath;
        try {
            imagePath = imageService.saveImage(form.getImage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Post post = postMapper.toPost(form, imagePath);
        postRepository.create(post);
    }

    @Override
    public Post getPostById(long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Post with id %s not found", id));
        }
        post.setImagePath(imageService.getImageSrcPath(post.getImagePath()));
        return post;
    }

}
