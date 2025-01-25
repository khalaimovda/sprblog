package com.github.khalaimovda.pagination;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PageRequest implements Pageable {

    private final int number;
    private final int size;

    @Override
    public int getPageNumber() {
        return number;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public int getOffset() {
        return number * size;
    }
}
