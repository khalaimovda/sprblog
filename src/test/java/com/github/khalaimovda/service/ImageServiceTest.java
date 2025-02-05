package com.github.khalaimovda.service;

import com.github.khalaimovda.config.ImageServiceTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = ImageServiceTestConfig.class)
@ActiveProfiles("test")
class ImageServiceTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Path imageLocation;

    @Autowired
    private ImageService imageService;

    @BeforeEach
    void cleanUpDirectory() throws IOException {
        if (Files.exists(imageLocation)) {
            Files.walk(imageLocation)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Clean test image directory error", e);
                    }
                });
        }
        Files.createDirectories(imageLocation);
    }

    @Test
    void testSaveImage() throws IOException {
        byte[] imageBytes = createRandomBytes();
        MultipartFile imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", imageBytes
        );

        String storedFilename = imageService.saveImage(imageFile);

        Path savedFilePath = imageLocation.resolve(storedFilename);
        assertTrue(Files.exists(savedFilePath));

        byte[] savedContent = Files.readAllBytes(savedFilePath);
        assertArrayEquals(imageBytes, savedContent);
    }

    @Test
    void testGetImagePath() {
        String filename = "test_image.jpg";

        Path correctImagePath = imageLocation.resolve(filename);
        Path imagePath = imageService.getImagePath(filename);
        assertEquals(correctImagePath, imagePath);
    }

    @Test
    void testGetImageSrcPath() {
        assertEquals("images/test_image.jpg", imageService.getImageSrcPath("test_image.jpg"));
    }

    @Test
    void testDeleteImage() throws IOException {
        byte[] imageBytes = createRandomBytes();
        Path imagePath = imageLocation.resolve("test_image.jpg");
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