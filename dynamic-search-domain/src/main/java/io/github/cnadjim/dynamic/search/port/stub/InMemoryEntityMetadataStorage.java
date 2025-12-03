package io.github.cnadjim.dynamic.search.port.stub;

import io.github.cnadjim.dynamic.search.model.EntityDescriptor;
import io.github.cnadjim.dynamic.search.model.FieldType;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;
import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class InMemoryEntityMetadataStorage implements EntityMetadataStorage {

    private final ConcurrentMap<Class<?>, EntityDescriptor> entityDescriptorConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public EntityDescriptor save(EntityDescriptor entityDescriptor) {
        entityDescriptorConcurrentMap.put(entityDescriptor.entityClass(), entityDescriptor);
        return entityDescriptor;
    }

    @Override
    public Optional<EntityDescriptor> findById(Class<?> id) {
        return Optional.ofNullable(entityDescriptorConcurrentMap.get(id));
    }

    @Override
    public void deleteById(Class<?> id) {
        entityDescriptorConcurrentMap.remove(id);
    }

    @Override
    public boolean existsById(Class<?> id) {
        return entityDescriptorConcurrentMap.containsKey(id);
    }

    @Override
    public FieldType resolveFieldType(Class<?> entityClass, String fieldKey) {

        Optional<EntityDescriptor> descriptorOpt = findById(entityClass);

        if (descriptorOpt.isEmpty()) {
            return FieldType.STRING;
        }

        EntityDescriptor descriptor = descriptorOpt.get();

        Optional<FilterDescriptor> optionalFilterDescriptor = descriptor.filters()
                .stream()
                .filter(predicate -> predicate.key().equals(fieldKey))
                .findFirst();

        if (optionalFilterDescriptor.isEmpty()) {
            return FieldType.STRING;
        }

        return optionalFilterDescriptor.get().fieldType();
    }
}
