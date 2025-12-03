package io.github.cnadjim.dynamic.search.spring.jpa.adapter;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.spring.starter.mapper.PageToSearchResultMapper;
import io.github.cnadjim.dynamic.search.spring.jpa.specification.GenericSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adaptateur de Repository générique - Implémente le port OUT défini dans le domaine
 * Fait le pont entre le domaine et l'infrastructure JPA
 * Retourne directement les entités JPA, le mapping vers le domaine se fait au niveau du use case si nécessaire
 *
 * Note: getAvailableFilters() a été supprimé car désormais géré par FilterMetadataExtractor
 * qui utilise les annotations @Searchable pour extraire les métadonnées
 *
 * @param <T>  Type de l'entité (peut être une entité JPA ou un objet du domaine)
 */
public class JpaEntityRepositoryAdapter<T> implements EntityRepository<T> {

    private final JpaSpecificationExecutor<T> specificationExecutor;

    public JpaEntityRepositoryAdapter(JpaSpecificationExecutor<T> specificationExecutor, EntityManager entityManager, Class<T> entityClass) {
        this.specificationExecutor = specificationExecutor;
    }

    @Override
    public SearchResult<T> findByCriteria(SearchCriteria criteria) {
        // Création de la spécification à partir des critères du domaine
        GenericSpecification<T> specification = new GenericSpecification<>(criteria);

        // Création du tri Spring Data depuis les critères de tri du domaine
        Sort sort = createSort(criteria.sorts());

        // Création de la pagination depuis les critères de number
        Pageable pageable = createPageable(criteria.page().number(), criteria.page().size(), sort);

        // Exécution de la requête JPA
        Page<T> page = specificationExecutor.findAll(specification, pageable);

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
