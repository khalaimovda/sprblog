package com.github.khalaimovda.pagination;

public interface Pageable {
    int getPageNumber();
    int getPageSize();
    int getOffset();

    static Pageable of(int number, int size) {
        return new PageRequest(number, size);
    }
}