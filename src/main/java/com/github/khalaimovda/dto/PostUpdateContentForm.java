package com.github.khalaimovda.dto;

import com.github.khalaimovda.model.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class PostUpdateContentForm {

    @NotNull
    private String title;
    @NotNull
    private String text;
    private MultipartFile image;
    private Set<Tag> tags;
}
