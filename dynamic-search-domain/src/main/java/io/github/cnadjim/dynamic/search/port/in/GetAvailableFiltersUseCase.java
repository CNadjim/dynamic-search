package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.model.FilterDescriptor;

import java.util.List;


public interface GetAvailableFiltersUseCase {

    <T> List<FilterDescriptor> getAvailableFilters(Class<T> entityClass);
}
