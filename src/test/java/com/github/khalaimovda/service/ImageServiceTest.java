package com.github.khalaimovda.service;

import com.github.khalaimovda.config.ImageServiceTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringJUnitConfig(classes = ImageServiceTestConfig.class)
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

    private byte[] createRandomBytes() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        return bytes;
    }
}