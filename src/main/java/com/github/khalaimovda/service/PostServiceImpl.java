package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Comment;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.github.khalaimovda.specification.PostSpecification.hasTag;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public Page<Post> getPosts(Pageable pageable, Tag tagFilter) {
        Specification<Post> filters = Specification
            .where(tagFilter == null ? null : hasTag(tagFilter));

        return postRepository.findAll(filters, pageable);
    }

    @Override
    @Transactional
    public Post createPost(String name) {
        Post post = new Post();
        post.setName(name);
        post.setText("Some random text + " + name);
        post.setTags(Set.of(Tag.ART, Tag.SCIENCE));

        Comment firstComment = new Comment();
        firstComment.setText("First comment");
        post.addComment(firstComment);

        Comment secondComment = new Comment();
        secondComment.setText("Second comment");
        post.addComment(secondComment);

        return postRepository.save(post);
    }
}
