package com.fenix.platform.util;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.Specification;

public final class SpecificationUtils {
    private SpecificationUtils() {
    }

    public static <T> Specification<T> and(Specification<T> base, Specification<T> next) {
        if (base == null) {
            return next;
        }
        if (next == null) {
            return base;
        }
        return base.and(next);
    }

    public static <T> Specification<T> equal(String field, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(path(root, field), value);
    }

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(path(root, field).as(String.class)), "%" + value.toLowerCase() + "%");
    }

    public static <T> Specification<T> between(String field, OffsetDateTime from, OffsetDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        return (root, query, cb) -> {
            jakarta.persistence.criteria.Path<OffsetDateTime> path = root.get(field);
            if (from != null && to != null) {
                return cb.between(path, from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(path, from);
            }
            return cb.lessThanOrEqualTo(path, to);
        };
    }

    private static jakarta.persistence.criteria.Path<Object> path(jakarta.persistence.criteria.Root<?> root, String field) {
        String[] parts = field.split("\\.");
        jakarta.persistence.criteria.Path<Object> current = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            current = current.get(parts[i]);
        }
        return current;
    }
}
