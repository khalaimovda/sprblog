package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostCreateForm {

    @NotNull
    private String title;
    @NotNull
    private String text;
    @NotNull
    private MultipartFile image;
    private Set<Tag> tags;
}
