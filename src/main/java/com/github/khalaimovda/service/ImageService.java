package com.github.khalaimovda.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


public interface ImageService {
    String saveImage(MultipartFile file);
    Path getImagePath(String fileName);
    String getImageSrcPath(String fileName);
    void deleteImage(String fileName);
}
