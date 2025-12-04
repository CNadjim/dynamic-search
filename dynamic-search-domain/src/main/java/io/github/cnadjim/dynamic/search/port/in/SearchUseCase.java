package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;


public interface SearchUseCase {

    <T> SearchResult<T> search(SearchCriteria criteria, Class<T> entityClass);
}
