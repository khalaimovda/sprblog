package com.github.khalaimovda.repository;


import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository {

    Page<Post> findAll(Pageable pageable);
}
