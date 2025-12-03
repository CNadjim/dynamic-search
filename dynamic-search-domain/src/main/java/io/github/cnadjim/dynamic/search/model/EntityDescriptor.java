package io.github.cnadjim.dynamic.search.model;

import java.util.List;

public record EntityDescriptor(
        Class<?> entityClass,
        List<FilterDescriptor> filters
) {

    public String getId() {
        return entityClass.getSimpleName();
    }
}
