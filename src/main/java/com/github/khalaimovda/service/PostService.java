package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateRequest;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<Post> getPosts(Pageable pageable, Tag tagFilter);
    Post createPost(PostCreateRequest request);
}
