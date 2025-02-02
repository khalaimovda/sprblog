package com.github.khalaimovda.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class ImageUtils {
    public static void cleanImages(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        Path imageLocation = Paths.get(resource.getURI());

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

    public static Path createImageFilePath(ResourceLoader resourceLoader, String imageFilename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        Path imageLocation = Paths.get(resource.getURI());
        return imageLocation.resolve(imageFilename);
    }
}
