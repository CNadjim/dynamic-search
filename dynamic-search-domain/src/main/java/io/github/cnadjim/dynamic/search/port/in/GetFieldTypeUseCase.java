package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.exception.ResourceNotFoundException;
import io.github.cnadjim.dynamic.search.model.FieldType;

import java.util.Optional;


public interface GetFieldTypeUseCase {

    default <T> FieldType getFieldTypeByKey(String key, Class<T> entityClass) {
        return findFieldTypeByKey(key, entityClass)
                .orElseThrow(() -> new ResourceNotFoundException("FieldType", key));
    }

    <T> Optional<FieldType> findFieldTypeByKey(String key, Class<T> entityClass);
}
