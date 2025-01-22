package com.github.khalaimovda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "comments")
@NoArgsConstructor
@Getter
@Setter
public class Comment extends BaseModel {

    @Column
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", text='" + text + '\'' +
            '}';
    }
}
