package com.fenix.platform.util;

import com.fenix.platform.config.PagingProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageableFactory {
    private final PagingProperties pagingProperties;

    public Pageable from(Integer page, Integer size, String sort) {
        int resolvedPage = page != null && page >= 0 ? page : 0;
        int resolvedSize = resolveSize(size);
        Sort resolvedSort = parseSort(sort);
        return PageRequest.of(resolvedPage, resolvedSize, resolvedSort);
    }

    private int resolveSize(Integer size) {
        int fallback = pagingProperties.getDefaultSize();
        int max = pagingProperties.getMaxSize();
        int resolved = size != null ? size : fallback;
        if (resolved < 1) {
            resolved = fallback;
        }
        return Math.min(resolved, max);
    }

    private Sort parseSort(String sort) {
        String effectiveSort = sort;
        if (effectiveSort == null || effectiveSort.isBlank()) {
            effectiveSort = pagingProperties.getDefaultSort();
        }
        if (effectiveSort == null || effectiveSort.isBlank()) {
            return Sort.unsorted();
        }
        String[] parts = effectiveSort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1].trim())
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
