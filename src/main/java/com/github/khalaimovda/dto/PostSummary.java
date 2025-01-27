package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.BaseModel;
import com.github.khalaimovda.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostSummary {
    private String title;
    private String text;
    private String imagePath;
    private int likes;
    private Set<Tag> tags;
    private int comments;
}
