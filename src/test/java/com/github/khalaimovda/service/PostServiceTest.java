package com.github.khalaimovda.service;

import com.github.khalaimovda.config.PostMapperTestConfig;
import com.github.khalaimovda.dto.*;
import com.github.khalaimovda.mapper.PostMapper;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.pagination.Page;
import com.github.khalaimovda.pagination.Pageable;
import com.github.khalaimovda.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = {PostServiceImpl.class, PostMapperTestConfig.class})
@ActiveProfiles("test")
class PostServiceTest {

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;

    @Captor
    private ArgumentCaptor<PostUpdateContentDto> postUpdateContentDtoCaptor;

    @Captor
    private ArgumentCaptor<String> imagePathCaptor;

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

    @Test
    void testLikePost() {
        doNothing().when(postRepository).incrementLikes(13L);
        postService.likePost(13L);
        verify(postRepository).incrementLikes(13L);
    }

    @Test
    void testDeletePost() {
        String prevImagePath = "path/to/prev/image.jpg";
        when(postRepository.deletePost(13L)).thenReturn(prevImagePath);
        doNothing().when(imageService).deleteImage(prevImagePath);

        postService.deletePost(13L);

        verify(postRepository).deletePost(13L);
        verify(imageService).deleteImage(prevImagePath);
    }

    @Test
    void testUpdatePostContentWithoutImage() {
        PostUpdateContentForm form = new PostUpdateContentForm();
        form.setTitle("New title");
        form.setText("New text");
        form.setTags(Set.of(Tag.POLITICS, Tag.SCIENCE));

        PostUpdateContentDto expectedDto = new PostUpdateContentDto(13L, form.getTitle(), form.getText(), form.getTags());

        doNothing().when(postRepository).updateContent(any());

        postService.updatePostContent(13L, form);

        verify(postRepository).updateContent(postUpdateContentDtoCaptor.capture());

        assertEquals(expectedDto, postUpdateContentDtoCaptor.getValue());
    }

    @Test
    void testUpdatePostContentWithImage() {
        String imageFilename = "updated_image.jpg";
        String imageContent = "Updated image content";
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            imageFilename,
            "image/jpeg",
            imageContent.getBytes()
        );
        String imagePath = "path/to/" + imageFilename;
        String prevImagePath = "path/to/prev/image.jpg";

        PostUpdateContentForm form = new PostUpdateContentForm();
        form.setTitle("New title");
        form.setText("New text");
        form.setTags(Set.of(Tag.ART));
        form.setImage(imageFile);

        PostUpdateContentDto expectedDto = new PostUpdateContentDto(13L, form.getTitle(), form.getText(), form.getTags());

        when(imageService.saveImage(imageFile)).thenReturn(imagePath);
        when(postRepository.updateContent(any(), anyString())).thenReturn(prevImagePath);
        doNothing().when(imageService).deleteImage(anyString());

        postService.updatePostContent(13L, form);

        verify(imageService).saveImage(imageFile);

        verify(postRepository).updateContent(postUpdateContentDtoCaptor.capture(), imagePathCaptor.capture());
        assertEquals(expectedDto, postUpdateContentDtoCaptor.getValue());
        assertEquals(imagePath, imagePathCaptor.getValue());

        verify(imageService).deleteImage(prevImagePath);
    }
}