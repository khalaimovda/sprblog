package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    Page<Post> getPosts(Pageable pageable);
    Post createPost(String name);
}
