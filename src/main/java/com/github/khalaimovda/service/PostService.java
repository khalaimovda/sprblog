package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Post;

import java.util.List;

public interface PostService {
    List<Post> getPosts();
    Post createPost(String name);
}
