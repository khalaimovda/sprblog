package com.github.khalaimovda.pagination;

import java.util.List;

public record DefaultPage<T>(int number, int totalPages, List<T> content) implements Page<T> {
}
