package com.github.khalaimovda.dto;


import com.github.khalaimovda.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostCreateDto {
    private String title;
    private String text;;
    private String imagePath;
    private Set<Tag> tags;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PostCreateDto createDto = (PostCreateDto) o;
        return Objects.equals(title, createDto.title) &&
            Objects.equals(text, createDto.text) &&
            Objects.equals(imagePath, createDto.imagePath) &&
            Objects.equals(tags, createDto.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, text, imagePath, tags);
    }
}
