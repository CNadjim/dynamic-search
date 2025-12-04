package io.github.cnadjim.dynamic.search.port.out;

import io.github.cnadjim.dynamic.search.model.EntityDescriptor;
import io.github.cnadjim.dynamic.search.model.Storage;

import java.util.Optional;

public interface EntityDescriptorStorage extends Storage<EntityDescriptor<?>, Class<?>> {
    <T> Optional<EntityDescriptor<T>> findByIdAndCast(Class<T> id);
}
