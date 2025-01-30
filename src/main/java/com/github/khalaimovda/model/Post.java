package com.github.khalaimovda.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Post extends BaseModel {

    private String title;
    private String text;
    private String imagePath;
    private int likes;
    private Set<Tag> tags;
    private List<Comment> comments;

    public Post(long id, String title, String text, String imagePath, int likes, Set<Tag> tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.likes = likes;
        this.tags = tags;
        this.comments = new ArrayList<>();
    }
}
