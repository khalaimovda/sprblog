package com.github.khalaimovda.service;

import com.github.khalaimovda.dto.*;
import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostMapper postMapper;

    @Override
    public Page<PostSummary> getPostSummaryPage(Pageable pageable, Tag tag) {
        Supplier<Tag> tagFilter = tag != null ? () -> tag : null;
        Page<PostSummary> page = postRepository.findAllSummariesPageable(pageable, tagFilter);
        page.content().forEach(ps -> ps.setImagePath(imageService.getImageSrcPath(ps.getImagePath())));
        return page;
    }

    @Override
    public void createPost(PostCreateForm form) {
        String imagePath = imageService.saveImage(form.getImage());
        PostCreateDto createDto = postMapper.toPostCreateDto(form, imagePath);
        postRepository.create(createDto);
    }

    @Override
    public Post getPostById(long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Post with id %s not found", id));
        }
        post.setImagePath(imageService.getImageSrcPath(post.getImagePath()));
        return post;
    }

    @Override
    public void addComment(long postId, String commentText) {
        postRepository.addComment(postId, commentText);
    }

    @Override
    public void likePost(long id) {
        postRepository.incrementLikes(id);
    }

    @Override
    public void deletePost(long id) {
        String imagePath = postRepository.deletePost(id);
        imageService.deleteImage(imagePath);
    }

    @Override
    public void updatePostContent(long id, PostUpdateContentForm form) {
        PostUpdateContentDto updateContentDto = postMapper.toPostUpdateContentDto(form, id);

        MultipartFile image = form.getImage();
        if (image != null && !image.isEmpty()) {
            updatePostContent(updateContentDto, image);
        } else {
            updatePostContent(updateContentDto);
        }
    }

    private void updatePostContent(PostUpdateContentDto dto, MultipartFile image) {
        String newImagePath = imageService.saveImage(image);

        String prevImagePath;
        try {
            prevImagePath = postRepository.updateContent(dto, newImagePath);
        } catch (Exception e) {
            imageService.deleteImage(newImagePath);
            return;
        }

        imageService.deleteImage(prevImagePath);
    }

    private void updatePostContent(PostUpdateContentDto dto) {
        postRepository.updateContent(dto);
    }
}
