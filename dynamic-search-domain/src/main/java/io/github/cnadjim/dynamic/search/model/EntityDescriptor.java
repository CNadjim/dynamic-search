package io.github.cnadjim.dynamic.search.model;

import java.util.List;

public record EntityDescriptor<T>(
        Class<T> entityClass,
        List<FilterDescriptor> filters,
        EntityRepository<T> entityRepository
) {
}
