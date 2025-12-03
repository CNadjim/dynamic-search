package io.github.cnadjim.dynamic.search.service;

import io.github.cnadjim.dynamic.search.metadata.FilterMetadataExtractor;
import io.github.cnadjim.dynamic.search.model.EntityDescriptor;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;
import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.port.in.GetAvailableFiltersUseCase;
import io.github.cnadjim.dynamic.search.port.in.RegisterEntityMetadataUseCase;
import io.github.cnadjim.dynamic.search.port.in.SearchUseCase;
import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.port.stub.InMemoryEntityMetadataStorage;

import java.util.List;

/**
 * Service du domaine générique - Implémente les cas d'usage de recherche et de découverte
 * Contient la logique métier pure, sans dépendances techniques
 * Orchestre les appels aux repositories via les ports OUT
 *
 * @param <T> Type de l'agrégat à rechercher
 */
public class SearchService<T> implements SearchUseCase<T>, RegisterEntityMetadataUseCase<T>, GetAvailableFiltersUseCase<T> {
    private final Class<T> entityClass;
    private final EntityRepository<T> entityRepository;
    private final EntityMetadataStorage entityMetadataStorage;

    public SearchService(EntityRepository<T> entityRepository, Class<T> entityClass) {
        this.entityRepository = entityRepository;
        this.entityClass = entityClass;
        this.entityMetadataStorage = new InMemoryEntityMetadataStorage();
        register(entityClass);
    }

    public SearchService(EntityRepository<T> entityRepository, EntityMetadataStorage entityMetadataStorage, Class<T> entityClass) {
        this.entityRepository = entityRepository;
        this.entityClass = entityClass;
        this.entityMetadataStorage = entityMetadataStorage;
        register(entityClass);
    }

    @Override
    public SearchResult<T> search(SearchCriteria criteria) {
        return entityRepository.findByCriteria(criteria);
    }

    @Override
    public List<FilterDescriptor> getAvailableFilters() {
        return entityMetadataStorage.getById(entityClass).filters();
    }

    @Override
    public void register(Class<T> entityClass) {
        final List<FilterDescriptor> filterDescriptors = FilterMetadataExtractor.extractFilters(entityClass);
        entityMetadataStorage.save(new EntityDescriptor(entityClass, filterDescriptors));
    }
}
