package com.github.khalaimovda.mapper;

import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.model.Post;
import org.mapstruct.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PostMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "likes", ignore = true),
        @Mapping(target = "comments", ignore = true),
        @Mapping(source = "image", target = "image", qualifiedByName = "multipartFileToByteArr")
    })
    Post toPost(PostCreateForm form);

    @Named("multipartFileToByteArr")
    default byte[] multipartFileToByteArr(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            System.err.println("Failed to convert MultipartFile to byte array: " + e.getMessage());
            return null;
        }
    }
}
