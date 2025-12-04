package io.github.cnadjim.dynamic.search.port.stub;

import io.github.cnadjim.dynamic.search.model.EntityDescriptor;
import io.github.cnadjim.dynamic.search.port.out.EntityDescriptorStorage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class InMemoryEntityDescriptorStorage implements EntityDescriptorStorage {

    private final ConcurrentMap<Class<?>, EntityDescriptor<?>> entityDescriptorConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public EntityDescriptor<?> save(EntityDescriptor<?> entityDescriptor) {
        entityDescriptorConcurrentMap.put(entityDescriptor.entityClass(), entityDescriptor);
        return entityDescriptor;
    }

    @Override
    public Optional<EntityDescriptor<?>> findById(Class<?> id) {
        return Optional.ofNullable(entityDescriptorConcurrentMap.get(id));
    }

    @Override
    public <T> Optional<EntityDescriptor<T>> findByIdAndCast(Class<T> id) {
        return findById(id).map(this::castEntityDescriptor);
    }

    @SuppressWarnings("unchecked")
    private <T> EntityDescriptor<T> castEntityDescriptor(EntityDescriptor<?> descriptor) {
        return (EntityDescriptor<T>) descriptor;
    }

    @Override
    public void deleteById(Class<?> id) {
        entityDescriptorConcurrentMap.remove(id);
    }

    @Override
    public boolean existsById(Class<?> id) {
        return entityDescriptorConcurrentMap.containsKey(id);
    }
}
