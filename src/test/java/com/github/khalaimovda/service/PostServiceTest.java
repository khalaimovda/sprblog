package com.github.khalaimovda.service;

import com.github.khalaimovda.config.TestConfig;
import com.github.khalaimovda.dto.PostCreateDto;
import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostSummary;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringJUnitConfig(classes = TestConfig.class)
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageService imageService;

    @BeforeEach
    void resetMocks() {
        reset(postRepository);
        reset(imageService);
    }

    @Test
    void testGetPostSummaryPage() {
        List<PostSummary> posts = List.of(
            new PostSummary(1L, "First", "First text", "first_path.jpg", 11, Set.of(Tag.SCIENCE, Tag.ART), 3),
            new PostSummary(2L, "Second", "Second text", "second_path.png", 2, Set.of(Tag.SCIENCE), 52)
        );
        Page<PostSummary> page = new Page<PostSummary>() {
            @Override
            public List<PostSummary> content() {
                return posts;
            }

            @Override
            public int number() {
                return 0;
            }

            @Override
            public int totalPages() {
                return 1;
            }
        };
        Pageable pageable = mock(Pageable.class);
        Tag tag = Tag.SCIENCE;

        when(postRepository.findAllSummariesPageable(pageable, tag)).thenReturn(page);
        when(imageService.getImageSrcPath(anyString()))
            .thenAnswer(invocation -> {
                String imagePath = invocation.getArgument(0);
                return "images/" + imagePath;
            });

        List<PostSummary> expectedPosts = List.of(
            new PostSummary(1L, "First", "First text", "images/first_path.jpg", 11, Set.of(Tag.SCIENCE, Tag.ART), 3),
            new PostSummary(2L, "Second", "Second text", "images/second_path.png", 2, Set.of(Tag.SCIENCE), 52)
        );

        Page<PostSummary> result = postService.getPostSummaryPage(pageable, tag);

        verify(postRepository, times(1)).findAllSummariesPageable(pageable, tag);
        verify(imageService, times(1)).getImageSrcPath("first_path.jpg");
        verify(imageService, times(1)).getImageSrcPath("second_path.png");

        assertNotNull(result);
        List<PostSummary> resultPosts = result.content();
        assertEquals(2, resultPosts.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedPosts.get(i), resultPosts.get(i));
        }
    }

    @Test
    void testCreatePost() {
        MultipartFile image = mock(MultipartFile.class);
        PostCreateForm form = new PostCreateForm("Test Title", "Test Text", image, Set.of(Tag.POLITICS, Tag.RELIGION));
        when(imageService.saveImage(image)).thenReturn("test_image_path.jpg");
        doNothing().when(postRepository).create(any(PostCreateDto.class));

        PostCreateDto expectedPostCreateDto = new PostCreateDto("Test Title", "Test Text", "test_image_path.jpg", Set.of(Tag.POLITICS, Tag.RELIGION));

        postService.createPost(form);

        verify(imageService).saveImage(image);

        ArgumentCaptor<PostCreateDto> postCreateDtoCaptor = ArgumentCaptor.forClass(PostCreateDto.class);
        verify(postRepository).create(postCreateDtoCaptor.capture());
        PostCreateDto capturedPostCreateDto = postCreateDtoCaptor.getValue();
        assertEquals(expectedPostCreateDto, capturedPostCreateDto);
    }

    @Test
    void testGetPostById() {
        Post post = new Post(13L, "Post Title", "Post Text", "image_name.jpg", 13, Set.of(Tag.POLITICS));
        when(postRepository.findById(13L)).thenReturn(post);
        when(imageService.getImageSrcPath("image_name.jpg")).thenReturn("/image/image_name.jpg");

        Post expectedPost = new Post(13L, "Post Title", "Post Text", "/image/image_name.jpg", 13, Set.of(Tag.POLITICS));

        Post resultPost = postService.getPostById(13L);
        verify(postRepository).findById(13L);
        verify(imageService).getImageSrcPath("image_name.jpg");

        assertEquals(expectedPost.getId(), resultPost.getId());
        assertEquals(expectedPost.getTitle(), resultPost.getTitle());
        assertEquals(expectedPost.getImagePath(), resultPost.getImagePath());
        assertEquals(expectedPost.getLikes(), resultPost.getLikes());
        assertEquals(expectedPost.getTags(), resultPost.getTags());
    }

    @Test
    void testGetPostByIdNotFound() {
        when(postRepository.findById(13L)).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> postService.getPostById(13L));
        assertEquals("Post with id 13 not found", exception.getReason());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(imageService, never()).getImageSrcPath(any());
    }
}