package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.Tag;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateContentDto {
    private Long id;
    private String title;
    private String text;
    private Set<Tag> tags;
}
