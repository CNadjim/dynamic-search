package io.github.cnadjim.dynamic.search.port.out;

import io.github.cnadjim.dynamic.search.model.EntityDescriptor;
import io.github.cnadjim.dynamic.search.model.FieldType;
import io.github.cnadjim.dynamic.search.model.Storage;

public interface EntityMetadataStorage extends Storage<EntityDescriptor, Class<?>> {
    FieldType resolveFieldType(Class<?> entityClass, String fieldKey);
}
