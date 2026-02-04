package com.fenix.platform.util;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.Specification;

public final class SpecificationBuilder<T> {
    private Specification<T> spec;

    private SpecificationBuilder() {
    }

    public static <T> SpecificationBuilder<T> builder() {
        return new SpecificationBuilder<>();
    }

    public SpecificationBuilder<T> equal(String field, Object value) {
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal(field, value));
        return this;
    }

    public SpecificationBuilder<T> likeIgnoreCase(String field, String value) {
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase(field, value));
        return this;
    }

    public SpecificationBuilder<T> between(String field, OffsetDateTime from, OffsetDateTime to) {
        spec = SpecificationUtils.and(spec, SpecificationUtils.between(field, from, to));
        return this;
    }

    public Specification<T> build() {
        return spec;
    }
}
