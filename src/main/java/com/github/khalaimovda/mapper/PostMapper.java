package com.github.khalaimovda.mapper;

import com.github.khalaimovda.dto.PostCreateDto;
import com.github.khalaimovda.dto.PostCreateForm;
import com.github.khalaimovda.dto.PostUpdateContentDto;
import com.github.khalaimovda.dto.PostUpdateContentForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PostMapper {

    @Mappings({
        @Mapping(target = "tags", defaultExpression = "java(new java.util.HashSet<Tag>())"),
        @Mapping(target = "imagePath", source = "imagePath")
    })
    PostCreateDto toPostCreateDto(PostCreateForm form, String imagePath);

    @Mappings({
        @Mapping(target = "tags", defaultExpression = "java(new java.util.HashSet<Tag>())"),
        @Mapping(target = "id", source = "id"),
    })
    PostUpdateContentDto toPostUpdateContentDto(PostUpdateContentForm form, long id);
}
