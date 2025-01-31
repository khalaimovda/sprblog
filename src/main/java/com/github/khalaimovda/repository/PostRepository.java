package com.github.khalaimovda.repository;


import com.github.khalaimovda.dto.PostCreateDto;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.dto.PostUpdateContentDto;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.function.Supplier;

@Repository
public interface PostRepository {
    Page<PostSummary> findAllSummariesPageable(Pageable pageable, @Nullable Tag tag);
    void create(PostCreateDto dto);
    @Nullable Post findById(long id);
    void addComment(long postId, String commentText);
    void updateContent(PostUpdateContentDto dto);
    String updateContent(PostUpdateContentDto dto, String imagePath);
    void incrementLikes(long id);
    void updateComment(long id, String text);
    String deletePost(long id);
    void deleteComment(long id);
}
