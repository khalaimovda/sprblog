package com.github.khalaimovda.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition="BLOB")
    private byte[] image;

    @Lob
    private String text;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int likes;

    @OneToMany(mappedBy="post")
    private Set<Comment> comments;

    public Post() {
    }


    @Override
    public String toString() {
        return "Post{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", text='" + text + '\'' +
            '}';
    }
}
