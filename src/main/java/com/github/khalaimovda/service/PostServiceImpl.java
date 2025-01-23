package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateRequest;
import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.khalaimovda.specification.PostSpecification.hasTag;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public Page<Post> getPosts(Pageable pageable, Tag tagFilter) {
        Specification<Post> filters = Specification
            .where(tagFilter == null ? null : hasTag(tagFilter));

        return postRepository.findAll(filters, pageable);
    }

    @Override
    @Transactional
    public Post createPost(PostCreateRequest request) {
        Post post = postMapper.toPost(request);
        return postRepository.save(post);
    }
}
