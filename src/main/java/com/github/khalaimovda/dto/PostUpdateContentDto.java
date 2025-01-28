package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PostUpdateContentDto {
    private Long id;
    private String title;
    private String text;
    private Set<Tag> tags;
}
