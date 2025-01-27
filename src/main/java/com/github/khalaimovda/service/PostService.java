package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;


public interface PostService {
    Page<PostSummary> getPosts(Pageable pageable, Tag tagFilter);
    void createPost(PostCreateForm form);
}
