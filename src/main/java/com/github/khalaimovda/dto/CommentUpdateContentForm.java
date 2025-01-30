package com.github.khalaimovda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentUpdateContentForm {
    @NotNull
    private String text;
}
