package com.github.khalaimovda.pagination;

import java.util.List;

public interface Page<T> {
    List<T> content();
    int number();
    int totalPages();

    static <T> Page<T> of(int number, int totalPages, List<T> content) {
        return new DefaultPage<>(number, totalPages, content);
    }
}