package io.github.cnadjim.dynamic.search.spring.mongo.adapter;

import io.github.cnadjim.dynamic.search.model.EntityRepository;
import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.spring.mongo.criteria.MongoCriteriaBuilder;
import io.github.cnadjim.dynamic.search.spring.starter.mapper.PageToSearchResultMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adaptateur de Repository générique pour MongoDB
 * Implémente le port OUT défini dans le domaine
 * Fait le pont entre le domaine et l'infrastructure MongoDB
 *
 * Note: getAvailableFilters() a été supprimé car désormais géré par FilterMetadataExtractor
 * qui utilise les annotations @Searchable pour extraire les métadonnées
 *
 * @param <T> Type de l'entité (document MongoDB)
 */
public class MongoEntityRepositoryAdapter<T> implements EntityRepository<T> {

    private final MongoTemplate mongoTemplate;
    private final Class<T> entityClass;

    public MongoEntityRepositoryAdapter(MongoTemplate mongoTemplate, Class<T> entityClass) {
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    @Override
    public SearchResult<T> findByCriteria(SearchCriteria criteria) {
        // Création de la Query MongoDB à partir des critères du domaine
        Query query = MongoCriteriaBuilder.buildQuery(criteria);

        // Création du tri Spring Data depuis les critères de tri du domaine
        Sort sort = createSort(criteria.sorts());

        // Création de la pagination depuis les critères de page
        Pageable pageable = createPageable(criteria.page().number(), criteria.page().size(), sort);

        // Application de la pagination à la query
        query.with(pageable);

        // Exécution de la requête MongoDB
        List<T> content = mongoTemplate.find(query, entityClass);

        // Compte total pour la pagination
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), entityClass);

        // Création de la Page
        Page<T> page = PageableExecutionUtils.getPage(content, pageable, () -> total);

        // Conversion du résultat vers le domaine via le mapper statique du starter
        return PageToSearchResultMapper.toSearchResult(page);
    }

    private Sort createSort(List<io.github.cnadjim.dynamic.search.model.SortCriteria> sortCriteria) {
        if (sortCriteria == null || sortCriteria.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortCriteria.stream()
                .map(criteria -> {
                    Sort.Direction direction = criteria.direction() == io.github.cnadjim.dynamic.search.model.SortDirection.ASC
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
                    return new Sort.Order(direction, criteria.key());
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
