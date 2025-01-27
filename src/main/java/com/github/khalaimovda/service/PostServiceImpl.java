package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostMapper postMapper;

    @Override
    public Page<PostSummary> getPosts(Pageable pageable, Tag tag) {
        Supplier<Tag> tagFilter = tag != null ? () -> tag : null;
        Page<PostSummary> posts = postRepository.findAll(pageable, tagFilter);
        posts.content().forEach(post -> post.setImagePath(imageService.getImageSrcPath(post.getImagePath())));
        return posts;
    }

    @Override
    public void createPost(PostCreateForm form) {
        String imagePath;
        try {
            imagePath = imageService.saveImage(form.getImage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Post post = postMapper.toPost(form, imagePath);
        postRepository.create(post);
    }


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
