package com.github.khalaimovda.service;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Primary
public class ImageServiceImpl implements ImageService {

    private final Path rootLocation;

    public ImageServiceImpl(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static/images");
        this.rootLocation = Paths.get(resource.getURI());
    }

    @Override
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path destinationFile = rootLocation.resolve(fileName).normalize();
        try {
            Files.copy(file.getInputStream(), destinationFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileName;
    }

    @Override
    public Path getImagePath(String fileName) {
        return rootLocation.resolve(fileName).normalize();
    }

    @Override
    public String getImageSrcPath(String filename) {
        return "images/" + filename;
    }

    @Override
    public void deleteImage(String fileName) {
        Path filePath = rootLocation.resolve(fileName).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
