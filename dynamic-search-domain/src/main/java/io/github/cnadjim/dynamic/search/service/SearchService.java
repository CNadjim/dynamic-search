package io.github.cnadjim.dynamic.search.service;

import io.github.cnadjim.dynamic.search.exception.ResourceNotFoundException;
import io.github.cnadjim.dynamic.search.metadata.FilterMetadataExtractor;
import io.github.cnadjim.dynamic.search.model.*;
import io.github.cnadjim.dynamic.search.port.in.GetFieldTypeUseCase;
import io.github.cnadjim.dynamic.search.port.in.GetAvailableFiltersUseCase;
import io.github.cnadjim.dynamic.search.port.in.RegisterEntityUseCase;
import io.github.cnadjim.dynamic.search.port.in.SearchUseCase;
import io.github.cnadjim.dynamic.search.port.out.EntityDescriptorStorage;

import java.util.Collections;
import java.util.List;


public class SearchService implements SearchUseCase, GetAvailableFiltersUseCase, GetFieldTypeUseCase, RegisterEntityUseCase {
    final EntityDescriptorStorage entityDescriptorStorage;

    public SearchService(EntityDescriptorStorage entityDescriptorStorage) {
        this.entityDescriptorStorage = entityDescriptorStorage;
    }

    @Override
    public <T> List<FilterDescriptor> getAvailableFilters(Class<T> entityClass) {
        return entityDescriptorStorage.findById(entityClass)
                .map(EntityDescriptor::filters)
                .orElse(Collections.emptyList());
    }

    @Override
    public <T> SearchResult<T> search(SearchCriteria criteria, Class<T> entityClass) {
        return entityDescriptorStorage.findByIdAndCast(entityClass)
                .map(entityDescriptor -> entityDescriptor.entityRepository().findByCriteria(criteria))
                .orElseThrow(() -> new ResourceNotFoundException("Entity not registered: " + entityClass.getName()));
    }

    @Override
    public <T> void registerEntity(Class<T> entityClass, EntityRepository<T> entityRepository) {
        List<FilterDescriptor> filterDescriptors = FilterMetadataExtractor.extractFilters(entityClass);
        EntityDescriptor<T> entityDescriptor = new EntityDescriptor<>(entityClass, filterDescriptors, entityRepository);
        entityDescriptorStorage.save(entityDescriptor);
    }

    @Override
    public <T> FieldType getFieldType(String fieldName, Class<T> entityClass) {
        return entityDescriptorStorage.findById(entityClass)
                .map(EntityDescriptor::filters)
                .orElse(Collections.emptyList())
                .stream()
                .filter(filterDescriptor -> filterDescriptor.key().equals(fieldName))
                .findFirst()
                .map(FilterDescriptor::fieldType)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldName + " in entity " + entityClass.getName()));
    }
}
