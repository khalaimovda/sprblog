package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.dto.PostUpdateContentForm;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;


public interface PostService {
    Page<PostSummary> getPostSummaryPage(Pageable pageable, Tag tagFilter);
    void createPost(PostCreateForm form);
    Post getPostById(long id);
    void updatePostContent(long id, PostUpdateContentForm form);
    void addComment(long postId, String commentText);
    void likePost(long id);
    void updateComment(long id, String text);
    void deletePost(long id);
    void deleteComment(long id);
}
