package com.github.khalaimovda.mapper;

import com.github.khalaimovda.dto.PostCreateRequest;
import com.github.khalaimovda.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PostMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "likes", ignore = true),
        @Mapping(target = "comments", ignore = true)
    })
    Post toPost(PostCreateRequest request);
}
