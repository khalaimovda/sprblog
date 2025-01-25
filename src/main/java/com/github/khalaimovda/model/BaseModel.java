package com.github.khalaimovda.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public abstract class BaseModel {

    protected Long id;
    private LocalDateTime createdAt;
}
