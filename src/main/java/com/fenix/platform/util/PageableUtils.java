package com.fenix.platform.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {
    private PageableUtils() {
    }

    public static Pageable from(Integer page, Integer size, String sort) {
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 50;
        Sort resolvedSort = parseSort(sort);
        return PageRequest.of(resolvedPage, resolvedSize, resolvedSort);
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
