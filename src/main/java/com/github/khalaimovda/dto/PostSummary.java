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
public class PostSummary {
    private Long id;
    private String title;
    private String text;
    private String imagePath;
    private int likes;
    private Set<Tag> tags;
    private int comments;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PostSummary that = (PostSummary) o;
        return likes == that.likes &&
            comments == that.comments &&
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(text, that.text) &&
            Objects.equals(imagePath, that.imagePath) &&
            Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, text, imagePath, likes, tags, comments);
    }
}
