package com.github.khalaimovda.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@Getter
@Setter
public class Post extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Lob
    private String text;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int likes;

    @Column
    @Enumerated
    @ElementCollection(
        targetClass = Tag.class,
        fetch = FetchType.EAGER
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(
        mappedBy="post",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    @Override
    public String toString() {
        return "Post{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", text='" + text + '\'' +
            ", imagePath='" + imagePath + '\'' +
            ", likes=" + likes +
            ", tags=" + tags +
            ", comments=" + comments +
            '}';
    }
}
