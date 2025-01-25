package com.github.khalaimovda.service;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

//import static com.github.khalaimovda.specification.PostSpecification.hasTag;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
//    private final PostMapper postMapper;

    @Override
    public Page<Post> getPosts(Pageable pageable, Tag tag) {
        Supplier<Tag> tagFilter = tag != null ? () -> tag : null;
        return postRepository.findAll(pageable, tagFilter);
    }

//    @Override
//    @Transactional
//    public Post createPost(PostCreateForm form, String imagePath) {
//        Post post = postMapper.toPost(form, imagePath);
//        return postRepository.save(post);
//    }


//    @Override
//    @Transactional
//    public Post createPost(PostCreateRequest createRequest) {
//        Post post = new Post();
//        post.setName(name);
//        post.setText("Some random text + " + name);
//        post.setTags(Set.of(Tag.ART, Tag.SCIENCE));
//
//        Comment firstComment = new Comment();
//        firstComment.setText("First comment");
//        post.addComment(firstComment);
//
//        Comment secondComment = new Comment();
//        secondComment.setText("Second comment");
//        post.addComment(secondComment);
//
//        return postRepository.save(post);
//    }
}
