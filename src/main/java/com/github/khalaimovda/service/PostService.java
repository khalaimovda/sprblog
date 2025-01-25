package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;


public interface PostService {
    Page<Post> getPosts(Pageable pageable, Tag tagFilter);
//    Post createPost(PostCreateForm form, String imagePath);
}
