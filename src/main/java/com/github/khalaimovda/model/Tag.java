package com.github.khalaimovda.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Tag {
    SCIENCE("science"),
    ART("art"),
    POLITICS("politics"),
    RELIGION("religion");

    private final String value;
}
