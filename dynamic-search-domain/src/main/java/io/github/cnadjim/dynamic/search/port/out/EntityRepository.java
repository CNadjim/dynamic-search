package io.github.cnadjim.dynamic.search.port.out;


import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;

public interface EntityRepository<T> {
    SearchResult<T> findByCriteria(SearchCriteria criteria);
}
