package io.github.cnadjim.dynamic.search.model;

import io.github.cnadjim.dynamic.search.port.out.EntityRepository;

import java.util.List;

public record EntityDescriptor<T>(
        Class<T> entityClass,
        List<FilterDescriptor> filters,
        EntityRepository<T> entityRepository
) {
}
