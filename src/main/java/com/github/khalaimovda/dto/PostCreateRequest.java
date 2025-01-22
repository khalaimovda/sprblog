package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.Tag;
//import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PostCreateRequest {

//    @NotNull
    private String name;
    private String text;
    private byte[] image;
    private Set<Tag> tags;
}
