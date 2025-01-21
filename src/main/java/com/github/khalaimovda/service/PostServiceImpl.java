package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    @Override
    @Transactional
    public Post createPost(String name) {
        Post post = new Post();
        post.setName(name);
        return postRepository.save(post);
    }
}
