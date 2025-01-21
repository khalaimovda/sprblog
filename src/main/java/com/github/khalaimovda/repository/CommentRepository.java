package com.github.khalaimovda.repository;

import com.github.khalaimovda.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
