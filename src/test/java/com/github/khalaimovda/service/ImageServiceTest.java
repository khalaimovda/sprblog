package com.github.khalaimovda.service;

import com.github.khalaimovda.config.ImageServiceProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = ImageServiceImpl.class)
@ActiveProfiles("test")
class ImageServiceTest {

    @Autowired
    private ImageServiceProperties properties;

    @Autowired
    private ImageService imageService;

    @BeforeEach
    void setup() throws IOException {
        Files.createDirectories(Paths.get(properties.getUploadDir()));
    }

    @AfterEach
    void cleanup() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(properties.getUploadDir()));
    }

    @Test
    void testSaveImage() throws IOException {
        byte[] imageBytes = createRandomBytes();
        MultipartFile imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", imageBytes
        );

        String storedFilename = imageService.saveImage(imageFile);

        Path savedFilePath = Paths.get(properties.getUploadDir()).resolve(storedFilename);
        assertTrue(Files.exists(savedFilePath));

        byte[] savedContent = Files.readAllBytes(savedFilePath);
        assertArrayEquals(imageBytes, savedContent);
    }

    @Test
    void testGetImagePath() {
        String filename = "test_image.jpg";

        Path correctImagePath = Paths.get(properties.getUploadDir()).resolve(filename);
        Path imagePath = imageService.getImagePath(filename);
        assertEquals(correctImagePath, imagePath);
    }

    @Test
    void testGetImageSrcPath() {
        assertEquals(properties.getBaseUrl() + "test_image.jpg", imageService.getImageSrcPath("test_image.jpg"));
    }

    @Test
    void testDeleteImage() throws IOException {
        byte[] imageBytes = createRandomBytes();
        Path imagePath = Paths.get(properties.getUploadDir()).resolve("test_image.jpg");
        Files.write(imagePath, imageBytes);
        assertTrue(Files.exists(imagePath));

        imageService.deleteImage("test_image.jpg");
        assertFalse(Files.exists(imagePath));
    }

    private byte[] createRandomBytes() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        return bytes;
    }
}