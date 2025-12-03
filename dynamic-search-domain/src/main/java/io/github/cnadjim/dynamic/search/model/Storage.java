package io.github.cnadjim.dynamic.search.model;


import io.github.cnadjim.dynamic.search.exception.ResourceNotFoundException;

import java.util.Optional;

public interface Storage<ITEM, IDENTIFIER> {

    ITEM save(ITEM item);

    Optional<ITEM> findById(IDENTIFIER id);

    void deleteById(IDENTIFIER id);

    boolean existsById(IDENTIFIER id);

    default ITEM getById(IDENTIFIER id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }
}
