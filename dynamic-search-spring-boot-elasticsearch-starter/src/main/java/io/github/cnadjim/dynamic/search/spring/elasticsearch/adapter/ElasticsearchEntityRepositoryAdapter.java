package io.github.cnadjim.dynamic.search.spring.elasticsearch.adapter;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.port.in.GetFieldTypeUseCase;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.spring.elasticsearch.criteria.ElasticsearchCriteriaBuilder;
import io.github.cnadjim.dynamic.search.spring.starter.mapper.PageToSearchResultMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adaptateur de Repository générique pour Elasticsearch
 * Implémente le port OUT défini dans le domaine
 * Fait le pont entre le domaine et l'infrastructure Elasticsearch
 *
 * @param <T> Type de l'entité (document Elasticsearch)
 */
public class ElasticsearchEntityRepositoryAdapter<T> implements EntityRepository<T> {

    private final Class<T> entityClass;

    private final GetFieldTypeUseCase getFieldTypeUseCase;
    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchEntityRepositoryAdapter(Class<T> entityClass, GetFieldTypeUseCase getFieldTypeUseCase, ElasticsearchOperations elasticsearchOperations) {
        this.entityClass = entityClass;
        this.getFieldTypeUseCase = getFieldTypeUseCase;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public SearchResult<T> findByCriteria(SearchCriteria criteria) {
        // Création de la Query Elasticsearch à partir des critères du domaine
        NativeQuery baseQuery = ElasticsearchCriteriaBuilder.buildQuery(criteria, entityClass);

        // Création du tri Spring Data depuis les critères de tri du domaine
        Sort sort = createSort(criteria.sorts());

        // Création de la pagination depuis les critères de number
        Pageable pageable = createPageable(criteria.page().number(), criteria.page().size(), sort);

        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder().withPageable(pageable);

        if (baseQuery.getQuery() != null) {
            nativeQueryBuilder.withQuery(baseQuery.getQuery());
        }

        NativeQuery nativeQuery = nativeQueryBuilder.build();

        // Exécution de la requête Elasticsearch
        SearchHits<T> searchHits = elasticsearchOperations.search(nativeQuery, entityClass);

        // Extraction du contenu
        List<T> content = searchHits.getSearchHits().stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());

        // Création de la Page
        Page<T> page = PageableExecutionUtils.getPage(content, pageable, searchHits::getTotalHits);

        // Conversion du résultat vers le domaine via le mapper statique du starter
        return PageToSearchResultMapper.toSearchResult(page);
    }

    private Sort createSort(List<io.github.cnadjim.dynamic.search.model.SortCriteria> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortCriteria.stream()
                .map(criteria -> {
                    String property = getFieldTypeUseCase.isFieldTypeString(criteria.key(), entityClass) ? criteria.key() + ".keyword" : criteria.key();
                    Sort.Direction direction = criteria.direction() == io.github.cnadjim.dynamic.search.model.SortDirection.ASC
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList());

        return Sort.by(orders);
    }

    private Pageable createPageable(Integer page, Integer size, Sort sort) {
        int pageNumber = Objects.requireNonNullElse(page, 0);
        int pageSize = Objects.requireNonNullElse(size, 100);
        return PageRequest.of(pageNumber, pageSize, sort);
    }

}
