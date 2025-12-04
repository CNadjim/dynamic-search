package io.github.cnadjim.dynamic.search.model;


public interface EntityRepository<T> {
    SearchResult<T> findByCriteria(SearchCriteria criteria);
}
