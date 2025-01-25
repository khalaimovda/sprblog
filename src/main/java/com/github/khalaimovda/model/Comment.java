package com.github.khalaimovda.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class Comment extends BaseModel {

    private String text;
    private Post post;
}
