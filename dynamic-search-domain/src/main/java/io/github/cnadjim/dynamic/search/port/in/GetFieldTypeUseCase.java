package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.model.FieldType;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;

import java.util.List;


public interface GetFieldTypeUseCase {

    <T> FieldType getFieldType(String fieldName, Class<T> entityClass);
}
