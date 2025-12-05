package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.port.out.EntityRepository;

public interface RegisterEntityUseCase {
    <T> void registerEntity(Class<T> entityClass, EntityRepository<T> entityRepository);
}
