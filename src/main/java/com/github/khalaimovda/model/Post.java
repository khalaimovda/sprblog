package com.github.khalaimovda.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Post extends BaseModel {

    private String title;
    private String text;
    private String imagePath;
    private int likes;
    // todo: comments; tags
}
